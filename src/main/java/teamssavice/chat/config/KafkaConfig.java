package teamssavice.chat.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import teamssavice.chat.property.KafkaProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name(kafkaProperties.chatTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
