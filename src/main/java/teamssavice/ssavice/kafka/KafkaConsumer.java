package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.chat.service.ChatService;

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
        if (MessageType.CREATE.equals(event.messageType()) && RoomType.DM.equals(event.roomType())) {
            chatService.connectRoomSinkForUser(event)
                    .doOnError(e -> System.out.println("RoomSink 연결 오류: " + e.getMessage()))
                    .subscribe();
            return;
        }
        chatService.sendMessageToLocalSubscribers(event);
    }

    @KafkaListener(
            topics = "${kafka.save-topic}",
            groupId = "chat-server-save-group"
    )
    public void listen(KafkaEvent.Save event) {
        chatService.saveChatMessage(event)
                .doOnError(e -> System.out.println("메시지 저장 실패: " + e.getMessage()))
                .block();
    }
}
