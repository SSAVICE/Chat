package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import teamssavice.ssavice.chat.ChatMessage;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chat.service.dto.ChatModel;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.chatmember.service.ChatMemberWriteService;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.kafka.KafkaProducer;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.redis.MessageIdGenerator;
import teamssavice.ssavice.room.infrastructure.RoomChannel;
import teamssavice.ssavice.room.service.RoomWriteService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageIdGenerator messageIdGenerator;
    private final KafkaProducer kafkaProducer;
    private final ChatWriteService chatWriteService;
    private final ChatReadService chatReadService;
    private final RoomWriteService roomWriteService;
    private final ChatMemberWriteService chatMemberWriteService;
    private final ChatMemberReadService chatMemberReadService;

    private final Map<String, RoomChannel> rooms = new ConcurrentHashMap<>();

    // 구독할 roomId를 emit하는 Sink (unicast - 초기 버퍼링 보장)
    private final Map<Long, Sinks.Many<String>> userSinks = new ConcurrentHashMap<>();

    // 구독 해제할 roomId를 emit하는 Sink (multicast - 방마다 개별 takeUntilOther 구독)
    private final Map<Long, Sinks.Many<String>> unsubscribeSinks = new ConcurrentHashMap<>();

    public Flux<ChatMessage> registerUser(Long subject) {
        Sinks.Many<String> userSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.Many<String> unsubscribeSink = Sinks.many().multicast().onBackpressureBuffer();

        userSinks.put(subject, userSink);
        unsubscribeSinks.put(subject, unsubscribeSink);

        // DB 조회를 체인과 분리해서 즉시 실행
        // → userSink.asFlux() 구독과 병렬로 시작
        // → WebSocket 연결 직후부터 동적 subscribe/unsubscribe 즉시 반영 가능
        Disposable initialLoad = chatMemberReadService.findAllBySubject(subject)
                .map(ChatMemberEntity::getRoomId)
                .doOnNext(userSink::tryEmitNext)
                .doOnError(e -> {
                    log.error("초기 채팅방 구독 조회 실패: subject={}, error={}", subject, e.getMessage(), e);
                    userSink.tryEmitError(e);
                })
                .subscribe();

        return userSink.asFlux()
                // scan: 구독 명령을 누적해서 현재 구독 중인 방 목록(상태)으로 변환
                // 초기값 {}도 emit되므로 buffer(2,1)의 첫 번째 쌍은 [{}, {A}]
                .scan(new HashSet<String>(), (activeRooms, roomId) -> {
                    HashSet<String> newRooms = new HashSet<>(activeRooms);
                    newRooms.add(roomId);
                    return newRooms;
                })
                // buffer(2, 1): [이전 상태, 현재 상태] 쌍으로 슬라이딩
                // → diff 계산으로 새로 추가된 방만 구독 (전체 재구독 방지)
                .buffer(2, 1)
                .filter(snapshots -> snapshots.size() == 2)
                .flatMap(snapshots -> {
                    Set<String> prev = snapshots.get(0);
                    Set<String> curr = snapshots.get(1);

                    // curr - prev = 새로 추가된 방만 구독
                    Set<String> added = new HashSet<>(curr);
                    added.removeAll(prev);

                    return Flux.fromIterable(added)
                            .flatMap(roomId ->
                                    rooms.computeIfAbsent(roomId, id -> new RoomChannel(id, rooms))
                                            .getFlux()
                                            // unsubscribeSink에서 이 roomId에 대한 해제 신호가 오면 구독 종료
                                            // → RoomChannel.subscriberCount 감소 → 0이면 rooms에서 자동 제거
                                            .takeUntilOther(
                                                    unsubscribeSink.asFlux()
                                                            .filter(id -> id.equals(roomId))
                                            )
                            );
                })
                .doFinally(sig -> {
                    initialLoad.dispose(); // DB 조회 구독 정리
                    userSinks.remove(subject);
                    unsubscribeSinks.remove(subject);
                    log.debug("유저 연결 종료: subject={}, signal={}", subject, sig);
                });
    }

    public Mono<Void> sendMessage(ChatCommand.Chat command, boolean isNewRoom) {
        return messageIdGenerator.nextMessageId(command.roomId())
                .flatMap(messageId ->
                        Mono.when(kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.from(messageId, command, isNewRoom)))
                ).doOnError(e -> log.error("Kafka 전송 실패: {}", e.getMessage(), e));
    }

    public Mono<Void> readMessage(ChatCommand.Read command) {
        return chatMemberWriteService.updateLastReadMsgIdIfGreater(command)
                .then(kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.from(command)))
                .doOnError(e -> log.error("Read 처리 실패: {}", e.getMessage(), e))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> sendMessageToLocalSubscribers(KafkaEvent.Chat event) {
        ChatMessage model = ChatMessage.builder()
                .messageId(event.messageId())
                .messageType(event.messageType())
                .roomType(event.roomType())
                .roomId(event.roomId())
                .receiver(event.receiver())
                .sender(event.sender())
                .message(event.message())
                .serviceId(event.serviceId())
                .createdAt(event.createdAt())
                .readMsgId(event.readMsgId())
                .build();

        String roomId = event.roomId();
        RoomChannel room = rooms.get(roomId);
        if (room != null) {
            room.emit(model);
        }
        return Mono.empty();
    }

    public Mono<Void> subscribeLocalUsersToRoom(String roomId) {
        return chatMemberReadService.findAllByRoomId(roomId)
                .map(ChatMemberEntity::getSubject)
                .filter(userSinks::containsKey)
                .distinct()
                .flatMap(subject -> subscribeUserToRoom(subject, roomId))
                .then();
    }

    // 새 채팅방 참여 시 userSink에 roomId emit → scan → buffer → flatMap 체인에서 즉시 반영
    public Mono<Void> subscribeUserToRoom(Long subject, String roomId) {
        Sinks.Many<String> userSink = userSinks.get(subject);
        if (userSink == null) return Mono.empty();

        return Mono.fromCallable(() -> userSink.tryEmitNext(roomId))
                .then();
    }

    // 채팅방 나가기 시 unsubscribeSink에 roomId emit → takeUntilOther가 감지 → 해당 방 구독 즉시 종료
    public Mono<Void> unsubscribeUserToRoom(Long subject, String roomId) {
        Sinks.Many<String> unsubscribeSink = unsubscribeSinks.get(subject);
        if (unsubscribeSink == null) return Mono.empty();

        return Mono.fromCallable(() -> unsubscribeSink.tryEmitNext(roomId))
                .then();
    }

    public Mono<Void> saveMessageAndUpdateRoom(KafkaEvent.Save event) {
        ChatCommand.Chat command = ChatCommand.Chat.builder()
                .messageId(event.messageId())
                .messageType(event.messageType())
                .roomType(event.roomType())
                .roomId(event.roomId())
                .receiver(event.receiver())
                .sender(event.sender())
                .message(event.message())
                .serviceId(event.serviceId())
                .createdAt(event.createdAt())
                .build();

        return chatWriteService.save(command)
                .then(roomWriteService.updateLastMsgId(event.roomId(), event.messageId(), event.createdAt(), event.message()));
    }

    public Flux<ChatModel.Message> getMessagesByCursor(Auth auth, ChatCommand.MessageCursor command) {
        return chatMemberReadService.validateChatMember(command.roomId(), auth.subject())
                .thenMany(
                        switch (command.direction()) {
                            case AFTER ->
                                    chatReadService.findMessagesAfterCursor(command.roomId(), command.cursor(), command.size());
                            case BEFORE ->
                                    chatReadService.findMessagesBeforeCursor(command.roomId(), command.cursor(), command.size());
                            case LATEST -> chatReadService.findLatestMessages(command.roomId(), command.size());
                        })
                .map(ChatModel.Message::from);
    }

    public boolean userConnectedLocally(Long subject) {
        return userSinks.containsKey(subject);
    }
}
