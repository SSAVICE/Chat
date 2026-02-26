package teamssavice.ssavice.room.controller.dto;

import lombok.Builder;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.service.dto.RoomModel;

import java.time.LocalDateTime;
import java.util.List;

public class RoomResponse {

    @Builder
    public record Rooms(
            List<Room> rooms
    ) {
        public static Rooms from(List<RoomModel.Room> model) {
            return Rooms.builder()
                    .rooms(model.stream().map(Room::from).toList())
                    .build();
        }
    }

    @Builder
    public record Room(
            String roomId,
            String roomName,
            RoomType roomType,
            Long serviceId,
            Long lastMsgId, // 마지막 채팅 Id
            Long unReadMsgCnt, // 안읽은 메시지 수
            int memberCnt,
            String lastMsg, // 마지막 메시지
            LocalDateTime lastMsgAt // // 마지막 메시지 시간
    ) {
        public static Room from(RoomModel.Room model) {
            return Room.builder()
                    .roomId(model.roomId())
                    .roomName(model.roomName())
                    .roomType(model.roomType())
                    .serviceId(model.serviceId())
                    .lastMsgId(model.lastMsgId())
                    .unReadMsgCnt(model.unReadMsgCnt())
                    .memberCnt(model.memberCnt())
                    .lastMsg(model.lastMsg())
                    .lastMsgAt(model.lastMsgAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String roomId,
            String roomName,
            RoomType roomType,
            Long serviceId,
            List<Long> lastReadMsgIds,
            List<Long> memberIds
    ) {
        public static Detail from(RoomModel.Detail model) {
            return Detail.builder()
                    .roomId(model.roomId())
                    .roomName(model.roomName())
                    .roomType(model.roomType())
                    .serviceId(model.serviceId())
                    .lastReadMsgIds(model.lastReadMsgIds())
                    .memberIds(model.memberIds())
                    .build();
        }
    }
}
