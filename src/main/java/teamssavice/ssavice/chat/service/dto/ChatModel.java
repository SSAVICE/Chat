package teamssavice.ssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.ssavice.room.constants.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

public class ChatModel {

    @Builder
    public record Room(
            String roomId,
            String roomName,
            RoomType roomType
    ) {
        public static ChatModel.Room from(RoomEntity entity) {
            return Room.builder()
                    .roomId(entity.getRoomId())
                    .roomName(entity.getRoomName())
                    .roomType(entity.getType())
                    .build();
        }

        public static ChatModel.Room from(RoomEntity entity, String roomName) {
            return Room.builder()
                    .roomId(entity.getRoomId())
                    .roomName(roomName)
                    .roomType(entity.getType())
                    .build();
        }
    }
}
