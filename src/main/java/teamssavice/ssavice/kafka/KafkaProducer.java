package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.property.KafkaProperties;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, KafkaEvent.Chat> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public Mono<Void> produce(KafkaEvent.Chat event) {
        System.out.println("produce: " + event.message());
        return Mono.fromFuture(
                kafkaTemplate.send(kafkaProperties.chatTopic(), event.roomId(), event)
            ).doOnError(e -> System.out.println("Kafka 전송 실패: " + e.getMessage()))
            .then();
    }
}
