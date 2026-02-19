package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publish(String topic, String key, Object payload) {
        return Mono.fromFuture(
                kafkaTemplate.send(topic, key, payload)
        ).then();
    }
}
