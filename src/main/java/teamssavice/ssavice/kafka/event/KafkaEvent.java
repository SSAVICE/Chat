package teamssavice.ssavice.kafka.event;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.chat.service.dto.ChatCommand;

import java.time.LocalDateTime;

public class KafkaEvent {

    @Builder
    public record Chat(
            MessageType messageType,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message,
            Long serviceId,
            LocalDateTime createdAt
    ) {

        public static Chat from(ChatCommand.Chat command) {
            return Chat.builder()
                    .messageType(command.messageType())
                    .roomType(command.roomType())
                    .roomId(command.roomId())
                    .receiver(command.receiver())
                    .sender(command.sender())
                    .message(command.message())
                    .serviceId(command.serviceId())
                    .createdAt(command.createdAt())
                    .build();
        }

        public static Chat createEvent(ChatCommand.Chat command) {
            return Chat.builder()
                    .messageType(MessageType.CREATE)
                    .roomId(command.roomId())
                    .roomType(command.roomType())
                    .createdAt(command.createdAt())
                    .build();
        }
    }

    @Builder
    public record Save(
            MessageType messageType,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message,
            Long serviceId,
            LocalDateTime createdAt
    ) {
        public static Save from(ChatCommand.Chat command) {
            return Save.builder()
                    .messageType(command.messageType())
                    .roomType(command.roomType())
                    .roomId(command.roomId())
                    .receiver(command.receiver())
                    .sender(command.sender())
                    .message(command.message())
                    .serviceId(command.serviceId())
                    .createdAt(command.createdAt())
                    .build();
        }
    }
}
