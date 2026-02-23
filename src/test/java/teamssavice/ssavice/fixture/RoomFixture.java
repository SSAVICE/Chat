package teamssavice.ssavice.fixture;

import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.time.LocalDateTime;

public class RoomFixture {

    public static RoomEntity room(String roomId, String roomName, Long lastMsgId) {
        return RoomEntity.builder()
                .roomId(roomId)
                .roomName(roomName)
                .type(RoomType.DM)
                .createdAt(LocalDateTime.now())
                .lastServiceId(null)
                .lastMsgId(lastMsgId)
                .build();
    }
}
