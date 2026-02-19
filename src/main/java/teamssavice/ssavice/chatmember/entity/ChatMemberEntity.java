package teamssavice.ssavice.chatmember.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("chat_member")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberEntity {
    @Id
    private Long id;

    @NotBlank
    private String roomId;

    @NotNull
    private String subject;

    @CreatedDate
    private LocalDateTime joinedAt;

    private boolean isLeft;

    @NotNull
    private Long lastReadMsgId;
}
