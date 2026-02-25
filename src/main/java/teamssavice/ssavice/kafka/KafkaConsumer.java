package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.service.ChatService;
import teamssavice.ssavice.kafka.event.KafkaEvent;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ChatService chatService;

    @KafkaListener(
            topics = "${kafka.chat-topic}",
            groupId = "chat-server-${kafka.server-id}"
    )
    public void listen(KafkaEvent.Chat event) {
        System.out.println("consume: " + event.message());
        if(MessageType.READ.equals(event.messageType())) {
            chatService.sendReadMessageToLocalSubscribers(event).block();
            return;
        }

        if(event.isNewRoom()) {
            chatService.subscribeLocalUsersToRoom(event.roomId()).block();
        }
        chatService.sendChatMessageToLocalSubscribers(event).subscribe();
    }

    @KafkaListener(
            topics = "${kafka.save-topic}",
            groupId = "chat-server-save-group"
    )
    public void listen(KafkaEvent.Save event) {
        chatService.saveMessageAndUpdateRoom(event)
                .block();
    }
}
