package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import teamssavice.ssavice.chat.constants.MessageType;
import teamssavice.ssavice.room.constants.RoomType;
import teamssavice.ssavice.chat.service.ChatService;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ChatService chatService;

    @KafkaListener(
            topics = "${kafka.chat-topic}",
            groupId = "chat-server-${kafka.server-id}"
    )
    public void consume(KafkaEvent.Chat event) {
        System.out.println("consume: " + event.message());
        if (MessageType.CREATE.equals(event.type()) && RoomType.DM.equals(event.roomType())) {
            chatService.connectRoomSinkForUser(event);
            return;
        }
        chatService.sendMessageToLocalSubscribers(event);
    }
}
