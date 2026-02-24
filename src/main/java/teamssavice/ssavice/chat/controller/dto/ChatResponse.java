package teamssavice.ssavice.chat.controller.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.service.dto.ChatModel;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    @Builder
    public record Messages(
        List<Message> messages
    ) {
        public static ChatResponse.Messages from(List<ChatModel.Message> models) {
            List<Message> list = models.stream().map(Message::from).toList();

            return Messages.builder()
                    .messages(list)
                    .build();
        }
    }

    @Builder
    public record Message(
        Long messageId,
        MessageType messageType,
        RoomType roomType,
        String roomId,
        Long sender,
        String message,
        LocalDateTime createdAt
    ) {
        public static ChatResponse.Message from(ChatModel.Message model) {
            return Message.builder()
                    .messageId(model.messageId())
                    .messageType(model.messageType())
                    .roomType(model.roomType())
                    .roomId(model.roomId())
                    .sender(model.sender())
                    .message(model.message())
                    .createdAt(model.createdAt())
                    .build();
        }
    }
}
