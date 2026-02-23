package teamssavice.ssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.time.LocalDateTime;

public class ChatModel {

    @Builder
    public record Room(
            String roomId,
            String roomName,
            RoomType roomType,
            Long serviceId,
            Long lastMsgId, // 마지막 채팅 Id
            Long unReadMsgCnt, // 안읽은 메시지 수
            String lastMessage, // 마지막 메시지
            Integer memberCnt,
            LocalDateTime lastMsgAt // 마지막 메시지 시간

    ) {
        public static ChatModel.Room from(RoomEntity entity, String roomName) {
            return Room.builder()
                    .roomId(entity.getRoomId())
                    .roomName(roomName)
                    .roomType(entity.getType())
                    .build();
        }

        public static ChatModel.Room of(RoomEntity room, ChatMessageEntity lastMessage, int memberCnt, Long lastReadMsgId) {

            return Room.builder()
                    .roomId(room.getRoomId())
                    .roomName(room.getRoomName())
                    .roomType(room.getType())
                    .serviceId(room.getLastServiceId())
                    .lastMsgId(room.getLastMsgId())
                    .unReadMsgCnt(room.getLastMsgId() - lastReadMsgId)
                    .lastMessage(lastMessage.getMessage())
                    .memberCnt(memberCnt)
                    .lastMsgAt(lastMessage.getCreatedAt())
                    .build();
        }
    }
}
