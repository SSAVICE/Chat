package teamssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import teamssavice.chat.model.ChatMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
public class ChatService {

    // 1. 모든 접속자의 개인 우편함 (Key: user ID, Value: 개인 Sink)
    private final Map<String, Sinks.Many<ChatMessage>> userSinks = new ConcurrentHashMap<>();

    // 2. 채팅방별 구독자 명단 (Key: Room ID, Value: Sender들의 집합) // DB에 저장 예정
    private final Map<String, Set<String>> roomSubscribers = new ConcurrentHashMap<>();

    // 3. 사용자별 참여중인 방 목록 (Key: User ID -> Set<Room ID>)
    private final Map<String, Set<String>> userRooms = new ConcurrentHashMap<>();

    // 사용자 접속 시 개인 우편함 생성
    public Flux<ChatMessage> registerUser(String userId) {
        Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(userId, sink);
        return sink.asFlux();
    }

    // 사용자가 특정 방을 구독(JOIN)
    public void joinRoom(String userId, String roomId) {
        roomSubscribers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(userId);

        userRooms.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(roomId);
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
