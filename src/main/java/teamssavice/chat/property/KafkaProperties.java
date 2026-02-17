package teamssavice.chat.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka")
public record KafkaProperties(
        String chatTopic
) {
}
