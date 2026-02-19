package teamssavice.ssavice.chat.websocket.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;

public class WebSocketRequest {

    @Builder
    public record Chat(
            MessageType messageType,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message,
            Long serviceId
    ) {
    }
}
