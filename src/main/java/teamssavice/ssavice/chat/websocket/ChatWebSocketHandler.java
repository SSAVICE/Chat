package teamssavice.ssavice.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.service.ChatService;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chat.websocket.dto.WebSocketRequest;
import teamssavice.ssavice.chatmember.service.ChatMemberService;

import java.net.URI;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatMemberService chatMemberService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 1. URL에서 sender 추출 (예: ws://.../chat?sender=철수)
        String subject = getSenderFromSession(session);

        // 1. 입력 처리 (Client -> Server)
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(json ->
                        Mono.fromCallable(() -> objectMapper.readValue(json, WebSocketRequest.class))
                                .onErrorResume(e -> {
                                    log.warn("잘못된 JSON 수신: {}", json, e);
                                    return Mono.empty(); // 이 메시지만 drop
                                })
                )
                .flatMap(req -> {
                    if(MessageType.READ.equals(req.getMessageType())) {
                        return chatMemberService.updateLastReadMsgId(ChatCommand.Read.from(req, subject));
                    }
                    return chatService.sendMessage(ChatCommand.Chat.from(req, subject));
                })
                .doFinally(signal -> log.info("Input Flux 종료: {}", signal))
                .then();

        // 2. 출력 처리: (Server -> Client)
        Flux<WebSocketMessage> output = chatService.registerUser(subject)
                .flatMap(message ->
                        Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(session::textMessage)
                                .onErrorResume(e -> {
                                    log.error("Kafka 전송 실패 roomId={}", message.getRoomId(), e);
                                    return Mono.empty();
                                })
                )
                .doFinally(signal -> log.info("output Flux 종료: {}", signal)) ;

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