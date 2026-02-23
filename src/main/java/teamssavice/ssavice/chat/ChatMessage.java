package teamssavice.ssavice.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private MessageType messageType;
    private RoomType roomType;
    private String roomId;
    private Long receiver; // roomType이 DM일 때만 receiver 존재
    private Long sender;
    private String message;
    private Long serviceId;
    private LocalDateTime createdAt;
    private Long[] readMsgIds;
}