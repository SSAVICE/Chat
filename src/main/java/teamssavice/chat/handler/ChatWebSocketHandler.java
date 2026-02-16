package teamssavice.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import teamssavice.chat.model.ChatMessage;
import teamssavice.chat.service.ChatService;

import java.net.URI;


@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 1. URL에서 sender 추출 (예: ws://.../chat?sender=철수)
        String userId = getSenderFromSession(session);

        // 1. 입력 처리 (Client -> Server)
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(json -> {
                    try {
                        ChatMessage message = objectMapper.readValue(json, ChatMessage.class);
                        message.setCreatedAt();

                        if(ChatMessage.MessageType.ENTER.equals(message.getType())) {
                            // 구독 요청
                            chatService.joinRoom(userId, message.getRoomId());
                            message.setMessage(message.getSender() + "님이 입장했습니다.");
                            chatService.sendMessage(message);
                        } else {
                            chatService.sendMessage(message);
                        }
                        return Mono.empty();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return Mono.error(e);
                    }
                })
                .then();

        // 2. 출력 처리: (Server -> Client)
        Flux<WebSocketMessage> output = chatService.registerUser(userId)
                .flatMap(message -> {
                    try {
                        String json = objectMapper.writeValueAsString(message);
                        return Mono.just(session.textMessage(json));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });

        return Mono.zip(input, session.send(output)).then();
    }

    // URL 쿼리 파라미터 파싱 헬퍼 메소드
    private String getSenderFromSession(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        String query = uri.getQuery(); // "sender=철수" 형태

        if (query != null && query.contains("sender=")) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "sender".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
}