package teamssavice.ssavice.room.controller.dto;

import lombok.Builder;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.chat.service.dto.ChatModel;

import java.util.List;

public class ChatResponse {

    @Builder
    public record Rooms(
            List<Room> rooms
    ) {
        public static Rooms from(List<ChatModel.Room> model) {
            return Rooms.builder()
                    .rooms(model.stream().map(Room::from).toList())
                    .build();
        }
    }

    @Builder
    public record Room(
            String roomId,
            String roomName,
            RoomType roomType
    ) {
        public static Room from(ChatModel.Room model) {
            return Room.builder()
                    .roomId(model.roomId())
                    .roomName(model.roomName())
                    .roomType(model.roomType())
                    .build();
        }
    }
}
