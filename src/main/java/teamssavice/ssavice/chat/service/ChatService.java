package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import teamssavice.ssavice.chat.entity.ChatMessage;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.chatmember.service.ChatMemberWriteService;
import teamssavice.ssavice.kafka.KafkaEvent;
import teamssavice.ssavice.kafka.KafkaProducer;
import teamssavice.ssavice.room.constants.RoomType;
import teamssavice.ssavice.room.service.RoomReadService;
import teamssavice.ssavice.room.service.RoomWriteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final KafkaProducer kafkaProducer;
    private final RoomWriteService roomWriteService;
    private final RoomReadService roomReadService;
    private final ChatMemberWriteService chatMemberWriteService;
    private final ChatMemberReadService chatMemberReadService;

    private final Map<String, Sinks.Many<ChatMessage>> userSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<ChatMessage>> roomSinks = new ConcurrentHashMap<>();

    // 사용자 접속 시 개인 우편함 생성
    public Flux<ChatMessage> registerUser(String subject) {
        Sinks.Many<ChatMessage> userSink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(subject, userSink);

        Flux<ChatMessage> output = userSink.asFlux()
                .doFinally(sig -> userSinks.remove(subject));

        chatMemberReadService.findAllBySubject(subject)
                .map(ChatMemberEntity::getRoomId)
                .flatMap(roomId -> connectRoomToUser(roomId, userSink))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> System.out.println("Room 연결 실패" + e.getMessage()))
                .subscribe();

         return output;
    }

    private Mono<Void> connectRoomToUser(String roomId, Sinks.Many<ChatMessage> userSink) {
        Sinks.Many<ChatMessage> roomSink = roomSinks.computeIfAbsent(roomId, id -> Sinks.many().multicast().onBackpressureBuffer());

        return roomSink.asFlux()
                .takeUntilOther(userSink.asFlux().ignoreElements())
                .doOnNext(userSink::tryEmitNext)
                .doOnError(sig -> System.out.println(sig))
                .doFinally(sig -> {
                    if (roomSink.currentSubscriberCount() == 0)
                        roomSinks.remove(roomId);
                }).then();
    }

    public Mono<Void> sendMessage(ChatCommand.Chat command) {
        return ensureRoomInMemory(command)
                .then(kafkaProducer.produce(KafkaEvent.Chat.from(command)))
                .doOnError(e -> System.out.println("Kafka 전송 실패: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> ensureRoomInMemory(ChatCommand.Chat command) {
        if(!RoomType.DM.equals(command.roomType())) return Mono.empty();

        return roomReadService.existsByRoomId(command.roomId())
                .flatMap(exist -> {
                    if (exist) return Mono.empty();
                    return createRoomIfNotExists(command.roomId(), command.createdAt())
                            .then(createChatMemberIfNotExists(command))
                            .then(kafkaProducer.produce(KafkaEvent.Chat.createEvent(command)));
                });
    }

    public Mono<Void> createRoomIfNotExists(String roomId, LocalDateTime createdAt) {
        return roomWriteService.save(roomId, createdAt)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public Mono<Void> createChatMemberIfNotExists(ChatCommand.Chat command) {
        List<String> subjects = List.of(command.sender(), command.receiver());

        return chatMemberWriteService.saveAll(subjects, command.roomId(), command.createdAt())
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public void sendMessageToLocalSubscribers(KafkaEvent.Chat event) {
        ChatMessage model = ChatMessage.builder()
                .type(event.type())
                .roomType(event.roomType())
                .roomId(event.roomId())
                .receiver(event.receiver())
                .sender(event.sender())
                .message(event.message())
                .serviceId(event.serviceId())
                .createdAt(event.createdAt())
                .build();

        String roomId = event.roomId();
        Sinks.Many<ChatMessage> roomSink = roomSinks.get(roomId);
        if(roomSink != null) {
            roomSink.tryEmitNext(model);
        }
    }

    public void connectRoomSinkForUser(KafkaEvent.Chat event) {
        chatMemberReadService.findAllByRoomId(event.roomId())
                .map(ChatMemberEntity::getSubject)
                .filter(userSinks::containsKey)
                .flatMap(subject -> connectRoomToUser(event.roomId(), userSinks.get(subject)))
                .subscribe(null, error -> System.out.println("RoomSink 연결 오류: " + error.getMessage()));
    }

}
