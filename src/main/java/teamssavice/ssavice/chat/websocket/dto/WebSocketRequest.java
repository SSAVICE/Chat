package teamssavice.ssavice.chat.websocket.dto;

import lombok.Builder;
import lombok.Getter;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;

@Getter
@Builder
public class WebSocketRequest {
    private MessageType messageType;
    private RoomType roomType;
    private String roomId;
    private Long receiver;
    private String message;
    private Long serviceId;
    private Long[] readMsgIds;
}
