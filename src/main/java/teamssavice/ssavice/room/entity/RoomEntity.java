package teamssavice.ssavice.room.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

@Table("room")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity {
    @Id
    private Long id;

    @NotBlank
    private String roomId;
    @NotNull
    private String roomName;

    @NotNull
    private RoomType type;

    @NotNull
    private LocalDateTime createdAt;

    private Long lastServiceId;
}
