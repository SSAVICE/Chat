package teamssavice.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private MessageType type;
    private RoomType roomType;
    private String roomId;
    private String receiver; // roomType이 DM일 때만 receiver 존재
    private String sender;
    private String message;
    private LocalDateTime createdAt;
}