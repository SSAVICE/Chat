package teamssavice.chat.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import teamssavice.chat.model.ChatMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class ChatService {


    // 1. 모든 접속자의 개인 우편함 (Key: user ID, Value: 개인 Sink)
    private final Map<String, Sinks.Many<ChatMessage>> userSinks = new ConcurrentHashMap<>();

    // 2. 채팅방별 구독자 명단 (Key: Room ID, Value: Session ID들의 집합)
    private final Map<String, Set<String>> roomSubscribers = new ConcurrentHashMap<>();

    // 사용자 접속 시 개인 우편함 생성
    public Flux<ChatMessage> registerUser(String userId) {
        Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(userId, sink);
        return sink.asFlux();
    }

    // 사용자가 특정 방을 구독(JOIN)
    public void joinRoom(String userId, String roomId) {
        roomSubscribers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(userId);
    }

    public void sendMessage(ChatMessage message) {
        String roomId = message.getRoomId();
        Set<String> subscribers = roomSubscribers.get(roomId);
        if(subscribers != null) {
            subscribers.forEach(userId -> {
                Sinks.Many<ChatMessage> sink = userSinks.get(userId);
                if(sink != null) sink.tryEmitNext(message);
            });
        }
    }
}
