package teamssavice.ssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

public class ChatModel {

    @Builder
    public record Message(
        Long messageId,
        MessageType messageType,
        RoomType roomType,
        String roomId,
        Long sender,
        String message,
        LocalDateTime createdAt,
        Long serviceId
    ) {
        public static ChatModel.Message from(ChatMessageEntity entity) {
            return Message.builder()
                    .messageId(entity.getMessageId())
                    .messageType(entity.getMessageType())
                    .roomType(entity.getRoomType())
                    .roomId(entity.getRoomId())
                    .sender(entity.getSender())
                    .message(entity.getMessage())
                    .createdAt(entity.getCreatedAt())
                    .serviceId(entity.getServiceId())
                    .build();
        }
    }
}
