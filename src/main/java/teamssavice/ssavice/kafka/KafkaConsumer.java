package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import teamssavice.ssavice.chat.service.ChatService;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.room.service.RoomService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ChatService chatService;
    private final RoomService roomService;

    @KafkaListener(
            topics = "${kafka.chat-topic}",
            groupId = "chat-server-${kafka.server-id}"
    )
    public void listen(KafkaEvent.Chat event) {
        log.info("consume: {}", event.message());
        if(event.isNewRoom()) {
            chatService.subscribeLocalUsersToRoom(event.roomId()).block();
        }

        chatService.sendMessageToLocalSubscribers(event).subscribe();
    }

    @KafkaListener(
            topics = "${kafka.save-topic}",
            groupId = "chat-server-save-group"
    )
    public void listen(KafkaEvent.Save event) {
        chatService.saveMessageAndUpdateRoom(event)
                .block();
    }

    @KafkaListener(
            topics = "${kafka.join-topic}",
            groupId = "chat-server-join-group"
    )
    public void listen(KafkaEvent.Join event) {
        roomService.createRoomIfNotExist(event.roomId(), event.roomName(), event.roomType(), List.of(event.sender()))
                .then(chatService.subscribeLocalUsersToRoom(event.roomId()))
                .block();
    }
}
