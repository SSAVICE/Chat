package teamssavice.ssavice.room.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            String lastMsg,
            LocalDateTime lastMsgAt // 마지막 메시지 시간

    ) {
        public static RoomModel.Room of(RoomEntity room, int memberCnt, Long lastReadMsgId, String roomName) {
            return Room.builder()
                    .roomId(room.getRoomId())
                    .roomName(roomName)
                    .roomType(room.getType())
                    .serviceId(room.getLastServiceId())
                    .lastMsgId(room.getLastMsgId())
                    .unReadMsgCnt(room.getLastMsgId() - lastReadMsgId)
                    .memberCnt(memberCnt)
                    .lastMsg(room.getLastMsg())
                    .lastMsgAt(room.getLastMsgAt() != null ? room.getLastMsgAt() : room.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String roomId,
            String roomName,
            RoomType roomType,
            Long serviceId,
            Map<Long, Long> members
    ) {
        public static RoomModel.Detail of(RoomEntity room, List<ChatMemberEntity> members, String roomName) {
            Map<Long, Long> states = new HashMap<>(members.size());
            for (ChatMemberEntity member : members) {
                states.put(member.getSubject(), member.getLastReadMsgId());
            }

            return Detail.builder()
                    .roomId(room.getRoomId())
                    .roomName(roomName)
                    .roomType(room.getType())
                    .serviceId(room.getLastServiceId())
                    .members(states)
                    .build();
        }
    }
}
