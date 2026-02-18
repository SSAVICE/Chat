package teamssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import teamssavice.chat.model.ChatMember;
import teamssavice.chat.model.ChatMessage;
import teamssavice.chat.model.MessageType;
import teamssavice.chat.model.Room;
import teamssavice.chat.repository.ChatMemberRepository;
import teamssavice.chat.repository.RoomRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RoomRepository roomRepository;
    private final ChatMemberRepository chatMemberRepository;

    // 1. 모든 접속자의 개인 우편함 (Key: user ID, Value: 개인 Sink)
    private final Map<String, Sinks.Many<ChatMessage>> userSinks = new ConcurrentHashMap<>();

    // 2. 채팅방별 구독자 명단 (Key: Room ID, Value: Sender들의 집합) // Redis라고 가정, Redis Set per user
    private final Map<String, Set<String>> roomSubscribers = new ConcurrentHashMap<>();

    // 3. 사용자별 참여중인 방 목록 (Key: User ID -> Set<Room ID>) // Redis라고 가정, Redis Set per user
    private final Map<String, Set<String>> userRooms = new ConcurrentHashMap<>();

    // 사용자 접속 시 개인 우편함 생성
    public Flux<ChatMessage> registerUser(String userId) {
        Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(userId, sink);
        return sink.asFlux();
    }

    public Mono<Void> ensureRoomInMemory(ChatMessage message) {
        if(roomSubscribers.containsKey(message.getRoomId())) return Mono.empty();

        return createRoomIfNotExists(message)
                .then(Mono.fromRunnable(() ->
                        roomSubscribers.computeIfAbsent(message.getRoomId(), k -> ConcurrentHashMap.newKeySet())
                            .addAll(List.of(message.getSender(), message.getReceiver()))
                ))
                .then(createChatMemberIfNotExists(message))
                .then(Mono.fromRunnable(() -> {
                    userRooms.computeIfAbsent(message.getReceiver(), k -> ConcurrentHashMap.newKeySet())
                            .add(message.getRoomId());
                    userRooms.computeIfAbsent(message.getSender(), k -> ConcurrentHashMap.newKeySet())
                            .add(message.getRoomId());
                }));
    }

    public Mono<Void> createRoomIfNotExists(ChatMessage message) {
        Room room = Room.builder()
                .roomId(message.getRoomId())
                .roomName("")
                .type(MessageType.TEXT)
                .createdAt(message.getCreatedAt())
                .build();

        return roomRepository.save(room)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public Mono<Void> createChatMemberIfNotExists(ChatMessage message) {
        List<String> userIds = List.of(message.getSender(), message.getReceiver());
        List<ChatMember> members = userIds.stream()
                .map(userId -> ChatMember.builder()
                        .roomId(message.getRoomId())
                        .userId(userId)
                        .joinedAt(message.getCreatedAt())
                        .isLeft(false)
                        .lastReadMsgId(0L)
                        .build())
                .toList();

        return chatMemberRepository.saveAll(members)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public void sendMessageToLocalSubscribers(ChatMessage message) {
        String roomId = message.getRoomId();
        Set<String> subscribers = roomSubscribers.get(roomId);
        if(subscribers != null) {
            subscribers.forEach(userId -> {
                Sinks.Many<ChatMessage> sink = userSinks.get(userId);
                if(sink != null) sink.tryEmitNext(message);
            });
        }
    }

    public void removeUser(String userId) {
        userSinks.remove(userId);
        Set<String> joinedRooms = userRooms.remove(userId);
        if(joinedRooms != null) {
            joinedRooms.forEach(roomId -> {
                Set<String> subscribers = roomSubscribers.get(roomId);
                if(subscribers != null) {
                    subscribers.remove(userId);
                    if(subscribers.isEmpty()) {
                        roomSubscribers.remove(roomId);
                    }
                }
            });
        }
    }
}
