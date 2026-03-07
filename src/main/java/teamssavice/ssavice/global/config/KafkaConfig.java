package teamssavice.ssavice.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
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

    @Bean
    public NewTopic syncTopic() {
        return TopicBuilder.name(kafkaProperties.syncTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        // 총 3회 실시(최초 + 2회 retry)
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // DLT
        handler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                JsonProcessingException.class
        );

        return handler;
    }
}
