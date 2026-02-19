package teamssavice.ssavice.kafka;

import lombok.Builder;
import teamssavice.ssavice.chat.constants.MessageType;
import teamssavice.ssavice.room.constants.RoomType;
import teamssavice.ssavice.chat.service.dto.ChatCommand;

import java.time.LocalDateTime;

public class KafkaEvent {

    @Builder
    public record Chat(
            MessageType type,
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
                    .type(command.type())
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
                    .type(MessageType.CREATE)
                    .roomId(command.roomId())
                    .roomType(command.roomType())
                    .createdAt(command.createdAt())
                    .build();
        }
    }
}
