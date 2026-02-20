package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import teamssavice.ssavice.chat.ChatMessage;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.chatmember.service.ChatMemberWriteService;
import teamssavice.ssavice.global.property.KafkaProperties;
import teamssavice.ssavice.kafka.KafkaProducer;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.service.RoomReadService;
import teamssavice.ssavice.room.service.RoomWriteService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final KafkaProperties kafkaProperties;

    private final ChatWriteService chatWriteService;
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
        return createDmRoomIfNotExist(command)
                .then(
                    Mono.when(
                        kafkaProducer.publish(kafkaProperties.chatTopic(), command.roomId(), KafkaEvent.Chat.from(command)),
                        kafkaProducer.publish(kafkaProperties.saveTopic(), command.roomId(), KafkaEvent.Save.from(command))
                    )
                ).doOnError(e -> System.out.println("Kafka 전송 실패: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> createDmRoomIfNotExist(ChatCommand.Chat command) {
        if(!RoomType.DM.equals(command.roomType())) return Mono.empty();
        String roomName = command.sender() + "_" + command.receiver();

        return roomReadService.existsByRoomId(command.roomId())
                .flatMap(exist -> {
                    if (exist) return Mono.empty();
                    return roomWriteService.save(command.roomId(), roomName, command.createdAt())
                            .then(chatMemberWriteService.saveAll(List.of(command.sender(), command.receiver()), command.roomId(), command.createdAt()))
                            .then(kafkaProducer.publish(kafkaProperties.chatTopic(), command.roomId(), KafkaEvent.Chat.createEvent(command)));
                });
    }

    public void sendMessageToLocalSubscribers(KafkaEvent.Chat event) {
        ChatMessage model = ChatMessage.builder()
                .messageType(event.messageType())
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

    public Mono<Void> connectRoomSinkForUser(KafkaEvent.Chat event) {
        return chatMemberReadService.findAllByRoomId(event.roomId())
                .map(ChatMemberEntity::getSubject)
                .filter(userSinks::containsKey)
                .flatMap(subject -> connectRoomToUser(event.roomId(), userSinks.get(subject)))
                .then();
    }

    public Mono<Void> saveChatMessage(KafkaEvent.Save event) {
        return chatWriteService.save(event);
    }
}
