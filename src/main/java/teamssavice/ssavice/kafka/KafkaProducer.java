package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.property.KafkaProperties;
import teamssavice.ssavice.kafka.event.KafkaEvent;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, KafkaEvent.Chat> kafkaChatTemplate;
    private final KafkaTemplate<String, KafkaEvent.Save> kafkaSaveTemplate;
    private final KafkaTemplate<String, KafkaEvent.Join> kafkaJoinTemplate;
    private final KafkaTemplate<String, KafkaEvent.Sync> kafkaSyncTemplate;

    public Mono<Void> publish(String key, KafkaEvent.Chat payload) {
        return Mono.fromFuture(
                kafkaChatTemplate.send(kafkaProperties.chatTopic(), key, payload)
        ).then();
    }

    public Mono<Void> publish(String key, KafkaEvent.Save payload) {
        return Mono.fromFuture(
                kafkaSaveTemplate.send(kafkaProperties.saveTopic(), key, payload)
        ).then();
    }

    public Mono<Void> publish(String key, KafkaEvent.Sync payload) {
        return Mono.fromFuture(
                kafkaSyncTemplate.send(kafkaProperties.syncTopic(), key, payload)
        ).then();
    }

    public Mono<Void> publish(String key, KafkaEvent.Join payload) {
        return Mono.fromFuture(
                kafkaJoinTemplate.send(kafkaProperties.joinTopic(), key, payload)
        ).then();
    }
}
