package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Long, Sinks.Many<String>> userSinks = new ConcurrentHashMap<>(); // User가 roomId를 emit하는 용도
    private final Map<String, RoomChannel> rooms = new ConcurrentHashMap<>();

    public Flux<ChatMessage> registerUser(Long subject) {
        Sinks.Many<String> userSink = Sinks.many().unicast().onBackpressureBuffer();
        userSinks.put(subject, userSink);

        chatMemberReadService.findAllBySubject(subject)
                .map(ChatMemberEntity::getRoomId)
                .subscribe(
                        userSink::tryEmitNext,
                        error -> System.out.println("userSink에 emit 실패: " + error)
                );

        return userSink.asFlux()
                .distinct()
                .flatMap(roomId -> rooms.computeIfAbsent(roomId, id -> new RoomChannel(id, rooms))
                        .getFlux()
                )
                .doFinally(sig -> userSinks.remove(subject));
    }

    public Mono<Void> sendMessage(ChatCommand.Chat command, boolean isNewRoom) {
        return messageIdGenerator.nextMessageId(command.roomId())
                    .flatMap(messageId ->
                        Mono.when(
                            kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.from(messageId, command, isNewRoom)),
                            kafkaProducer.publish(command.roomId(), KafkaEvent.Save.from(messageId, command))
                        )
                        ).doOnError(e -> System.out.println("Kafka 전송 실패: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> readMessage(ChatCommand.Read command) {
        return chatMemberWriteService.updateLastReadMsgIdIfGreater(command)
                .then(kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.from(command)))
                .doOnError(e -> System.out.println("Read 처리 실패: " + e.getMessage()))
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
        if(room != null) {
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

    public Mono<Void> subscribeUserToRoom(Long subject, String roomId) {
        Sinks.Many<String> userSink = userSinks.get(subject);
        if(userSink == null) return Mono.empty();

        return Mono.fromCallable(() -> userSink.tryEmitNext(roomId))
                .then();
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public Flux<ChatModel.Message> getMessagesByCursor(Auth auth, ChatCommand.MessageCursor command
    ) {
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
}
