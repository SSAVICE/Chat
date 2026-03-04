package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLTConsumer {
    private final DiscordNotifier discordNotifier;

    @KafkaListener(topics = {
            "${kafka.chat-topic}.DLT",
            "${kafka.save-topic}.DLT",
            "${kafka.join-topic}.DLT",
    },
    groupId = "chat-dlt-group"
    )
    public void handleDlt(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
            @Header(KafkaHeaders.DLT_EXCEPTION_STACKTRACE) String stackTrace
    ) {
        String shortTrace = stackTrace.length() > 1000
                ? stackTrace.substring(0, 1000) + "..."
                : stackTrace;

        String alert = """
        DLT 수신
        원본 토픽: %s
        예외: %s
        메시지: %s
        ShortTrace: %s
        """.formatted(originalTopic, exceptionMessage, message, shortTrace);

        try {
            log.error(alert);
            discordNotifier.sendDltAlert(originalTopic, exceptionMessage, shortTrace);
        } catch (Exception e) {
            log.error("Discord 실패", e);
        }
    }
}
