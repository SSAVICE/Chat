package teamssavice.chat.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import teamssavice.chat.model.ChatMessage;
import teamssavice.chat.service.ChatService;

@Component
@RequiredArgsConstructor
public class ChatKafkaConsumer {

    private final ChatService chatService;

    @KafkaListener(
            topics = "${kafka.chat-topic}",
            groupId = "chat-server-${kafka.server-id}"
    )
    public void consume(ChatMessage message) {
        System.out.println("consume: " + message.getMessage());
        chatService.sendMessageToLocalSubscribers(message);
    }
}
