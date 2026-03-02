package teamssavice.ssavice.global.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka")
public record KafkaProperties(
        String chatTopic,
        String saveTopic,
        String joinTopic
) {
}
