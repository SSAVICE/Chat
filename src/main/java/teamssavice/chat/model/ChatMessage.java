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
    public enum MessageType {
        ENTER, TALK // ENTER: 방 구독 요청, TALK: 대화
    }
    private MessageType type;
    private String roomId;
    private String sender;
    private String message;
    private LocalDateTime createdAt;

    public void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }

    public void setMessage(String message) {
        this.message = message;
    }
}