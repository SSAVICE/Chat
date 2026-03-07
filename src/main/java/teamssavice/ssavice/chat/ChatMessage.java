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
    private Long messageId;
    private MessageType messageType;
    private RoomType roomType;
    private String roomId;
    private Long receiver; // roomType이 DM일 때만 receiver 존재
    private Long sender;
    private String message;
    private Long serviceId;
    private LocalDateTime createdAt;
    private Long readMsgId;

    public static ChatMessage createErrorMessage(String roomId) {
        return ChatMessage.builder()
                .messageType(MessageType.ERROR)
                .roomId(roomId)
                .message("메시지 전송에 실패했습니다. 다시 시도해주세요.")
                .build();
    }
}