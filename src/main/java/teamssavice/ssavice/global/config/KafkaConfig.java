package teamssavice.ssavice.global.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import teamssavice.ssavice.global.property.KafkaProperties;

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

    @Bean
    public NewTopic saveTopic() {
        return TopicBuilder.name(kafkaProperties.saveTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic joinTopic() {
        return TopicBuilder.name(kafkaProperties.joinTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
