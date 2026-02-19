package teamssavice.ssavice.chat.websocket.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.constants.MessageType;
import teamssavice.ssavice.room.constants.RoomType;

public class WebSocketRequest {

    @Builder
    public record Chat(
            MessageType type,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message,
            Long serviceId
    ) {
    }
}
