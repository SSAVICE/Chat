package teamssavice.chat.websocket.dto;

import lombok.Builder;
import teamssavice.chat.model.MessageType;
import teamssavice.chat.model.RoomType;

public class WebSocketRequest {

    @Builder
    public record Chat(
            MessageType type,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message
    ) {
    }
}
