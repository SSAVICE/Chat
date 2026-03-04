package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import teamssavice.ssavice.global.property.DiscordProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiscordNotifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private final DiscordProperties discordProperties;

    public void sendDltAlert(String originalTopic, String exceptionMessage, String stackTrace) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "Kafka DLT 발생");
        embed.put("color", 16711680); // 빨간색

        embed.put("fields", List.of(
                Map.of(
                        "name", "Original Topic",
                        "value", originalTopic,
                        "inline", false
                ),
                Map.of(
                        "name", "Exception Message",
                        "value", exceptionMessage,
                        "inline", false
                ),
                Map.of(
                        "name", "Stack Trace",
                        "value", "```" + stackTrace + "```",
                        "inline", false
                )
        ));

        embed.put("footer", Map.of(
                "text", LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        Map<String, Object> body = Map.of(
                "embeds", List.of(embed)
        );
        restTemplate.postForEntity(discordProperties.webhookUrl(), body, String.class);
    }
}
