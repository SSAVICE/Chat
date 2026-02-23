package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import teamssavice.ssavice.chat.ChatMessage;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.chatmember.service.ChatMemberWriteService;
import teamssavice.ssavice.kafka.KafkaProducer;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.redis.MessageIdGenerator;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.infrastructure.RoomChannel;
import teamssavice.ssavice.room.service.RoomReadService;
import teamssavice.ssavice.room.service.RoomWriteService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageIdGenerator messageIdGenerator;
    private final ChatWriteService chatWriteService;
    private final KafkaProducer kafkaProducer;
    private final RoomWriteService roomWriteService;
    private final RoomReadService roomReadService;
    private final ChatMemberWriteService chatMemberWriteService;
    private final ChatMemberReadService chatMemberReadService;

    private final Map<Long, Sinks.Many<ChatMessage>> userSinks = new ConcurrentHashMap<>();
    private final Map<String, RoomChannel> rooms = new ConcurrentHashMap<>();

    public Flux<ChatMessage> registerUser(Long subject) {
        Sinks.Many<ChatMessage> userSink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(subject, userSink);

        Flux<ChatMessage> output = userSink.asFlux()
                .doFinally(sig -> userSinks.remove(subject));

        return chatMemberReadService.findAllBySubject(subject)
                .map(ChatMemberEntity::getRoomId)
                .flatMap(roomId -> connectRoomToUser(roomId, userSink))
                .thenMany(output);
    }

    private Mono<Void> connectRoomToUser(String roomId, Sinks.Many<ChatMessage> userSink) {
        RoomChannel room = rooms.computeIfAbsent(roomId, id -> new RoomChannel(id, rooms));
        Flux<ChatMessage> roomFlux = room.getFlux();

        roomFlux
            .takeUntilOther(userSink.asFlux().ignoreElements())
            .doOnNext(userSink::tryEmitNext)
            .doOnError(sig -> System.out.println(sig))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        return Mono.empty();
    }

    public Mono<Void> sendMessage(ChatCommand.Chat command) {
        return createDmRoomIfNotExist(command)
                .then(messageIdGenerator.nextMessageId(command.roomId()))
                .flatMap(messageId ->
                        Mono.when(
                                kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.from(messageId, command)),
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

    public Mono<Void> createDmRoomIfNotExist(ChatCommand.Chat command) {
        if(!RoomType.DM.equals(command.roomType())) return Mono.empty();
        String roomName = command.sender() + "_" + command.receiver();

        return roomReadService.existsByRoomId(command.roomId())
                .flatMap(exist -> {
                    if (exist) return Mono.empty();
                    return roomWriteService.save(command.roomId(), roomName, command.createdAt())
                            .then(chatMemberWriteService.saveAll(List.of(command.sender(), command.receiver()), command.roomId(), command.createdAt()))
                            .then(kafkaProducer.publish(command.roomId(), KafkaEvent.Chat.createEvent(command)));
                });
    }

    public void sendChatMessageToLocalSubscribers(KafkaEvent.Chat event) {
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
                .build();

        String roomId = event.roomId();
        RoomChannel room = rooms.get(roomId);
        if(room != null) {
            room.emit(model);
        }
    }

    public void sendReadMessageToLocalSubscribers(KafkaEvent.Chat event) {
        ChatMessage model = ChatMessage.builder()
                .messageType(event.messageType())
                .roomId(event.roomId())
                .sender(event.sender())
                .readMsgIds(event.readMsgIds())
                .build();

        String roomId = event.roomId();
        RoomChannel room = rooms.get(roomId);
        if(room != null) {
            room.emit(model);
        }
    }

    public Mono<Void> connectRoomSinkForUser(KafkaEvent.Chat event) {
        return chatMemberReadService.findAllByRoomId(event.roomId())
                .map(ChatMemberEntity::getSubject)
                .filter(userSinks::containsKey)
                .flatMap(subject -> connectRoomToUser(event.roomId(), userSinks.get(subject)))
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
                .then(roomWriteService.updateLastMsgId(event.roomId(), event.messageId()));
    }
}
