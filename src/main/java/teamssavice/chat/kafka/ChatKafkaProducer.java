package teamssavice.chat.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import teamssavice.chat.model.ChatMessage;
import teamssavice.chat.property.KafkaProperties;

@Component
@RequiredArgsConstructor
public class ChatKafkaProducer {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public Mono<Void> send(ChatMessage message) {
        System.out.println("produce: " + message.getMessage());
        return Mono.fromFuture(
                kafkaTemplate.send(kafkaProperties.chatTopic(), message.getRoomId(), message)
            ).then();
    }
}
