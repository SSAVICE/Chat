package teamssavice.ssavice.chat.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

@Table("chat_message")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {
    @Id
    private Long id;

    @NotNull
    private Long messageId;

    @NotBlank
    private MessageType messageType;
    @NotBlank
    private RoomType roomType;
    @NotBlank
    private String roomId;

    private Long receiver;
    @NotNull
    private Long sender;

    private String message;

    private Long serviceId;

    @NotNull
    private LocalDateTime createdAt;
}
