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
import reactor.core.publisher.Sinks;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.service.ChatService;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chat.websocket.dto.WebSocketRequest;
import teamssavice.ssavice.room.service.RoomService;

import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final RoomService roomService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Long> subjectMono = Mono.deferContextual(ctx ->
                Mono.just(ctx.get("subject"))
        );

        return subjectMono.flatMap(subject -> {
            // 클라이언트로부터 온 pong을 output 스트림으로 보내기 위한 Sink
            Sinks.Many<WebSocketMessage> pongSink = Sinks.many().unicast().onBackpressureBuffer();

            // 1. 입력 처리 (Client -> Server)
            Mono<Void> input = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .timeout(Duration.ofSeconds(60)) // 60초 동안 메시지가 없으면 연결 종료
                    .flatMap(json -> {
                        // PING을 수신하면 PONG을 Sink로 넣고 Json 파싱 건너 뜀
                        if ("PING".equalsIgnoreCase(json)) {
                            pongSink.tryEmitNext(session.textMessage("PONG"));
                            return Mono.empty();
                        }

                        return parseJsonToRequestDto(json)
                                .flatMap(req -> {
                                    if (MessageType.READ.equals(req.getMessageType())) {
                                        return chatService.readMessage(ChatCommand.Read.from(req, subject));
                                    }
                                    ChatCommand.Chat command = ChatCommand.Chat.from(req, subject);

                                    return roomService.createDMRoomIfNotExist(command)
                                            .flatMap(isNewRoom -> chatService.sendMessage(command, isNewRoom));
                                });
                    })
                    .doOnError(TimeoutException.class, e -> log.info("User {} 세션 타임아웃 - 연결 종료", subject))
                    .doFinally(signal -> log.info("Input Flux 종료: {}", signal))
                    .then();

            // 2. 출력 처리: (Server -> Client)
            Flux<WebSocketMessage> output = chatService.registerUser(subject)
                    .flatMap(message ->
                            Mono.<String>fromCallable(() -> objectMapper.writeValueAsString(message))
                                    .map(session::textMessage)
                                    .onErrorResume(e -> {
                                        log.error("Kafka 전송 실패 roomId={}", message.getRoomId(), e);
                                        return Mono.empty();
                                    })
                    )
                    .doFinally(signal -> log.info("output Flux 종료: {}", signal));

            // 채팅 메시지(output)와 PONG 응답(pongSink)를 하나로 병합
            Flux<WebSocketMessage> combinedOutput = Flux.merge(output, pongSink.asFlux());
            // 병합된 combinedOutput을 전송
            return Mono.when(input, session.send(combinedOutput));
        });
    }

    private Mono<WebSocketRequest> parseJsonToRequestDto(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, WebSocketRequest.class))
                .onErrorResume(e -> {
                    log.warn("잘못된 JSON 수신: {} {}", json, e.getMessage());
                    return Mono.empty(); // 이 메시지만 drop
                });
    }
}