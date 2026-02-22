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

    public Long getOppSubject(Long subject) {
        String[] arr = roomName.split("_");
        long id1 = Long.parseLong(arr[0]);
        long id2 = Long.parseLong(arr[1]);

        return subject == id1 ? id2 : id1;
    }
}
