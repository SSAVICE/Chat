package teamssavice.ssavice.room.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomModel {

    @Builder
    public record Room(
            String roomId,
            String roomName,
            RoomType roomType,
            Long serviceId,
            Long lastMsgId, // 마지막 채팅 Id
            Long unReadMsgCnt, // 안읽은 메시지 수
            Integer memberCnt,
            LocalDateTime lastMsgAt // 마지막 메시지 시간

    ) {
        public static RoomModel.Room from(RoomEntity entity, String roomName) {
            return Room.builder()
                    .roomId(entity.getRoomId())
                    .roomName(roomName)
                    .roomType(entity.getType())
                    .build();
        }

        public static RoomModel.Room of(RoomEntity room, int memberCnt, Long lastReadMsgId) {

            return Room.builder()
                    .roomId(room.getRoomId())
                    .roomName(room.getRoomName())
                    .roomType(room.getType())
                    .serviceId(room.getLastServiceId())
                    .lastMsgId(room.getLastMsgId())
                    .unReadMsgCnt(room.getLastMsgId() - lastReadMsgId)
                    .memberCnt(memberCnt)
                    .lastMsgAt(room.getLastMsgAt())
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
        public static RoomModel.Detail of(RoomEntity room, List<ChatMemberEntity> members) {
            List<Long> memberIds = new ArrayList<>(members.size());
            List<Long> lastReadMsgIds = new ArrayList<>(members.size());
            for (ChatMemberEntity member : members) {
                memberIds.add(member.getSubject());
                lastReadMsgIds.add(member.getLastReadMsgId());
            }

            return RoomModel.Detail.builder()
                    .roomId(room.getRoomId())
                    .roomName(room.getRoomName())
                    .roomType(room.getType())
                    .serviceId(room.getLastServiceId())
                    .memberIds(memberIds)
                    .lastReadMsgIds(lastReadMsgIds)
                    .build();
        }
    }
}
