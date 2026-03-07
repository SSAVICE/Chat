package teamssavice.ssavice.kafka.event;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

public class KafkaEvent {

    @Builder
    public record Chat(  // 메시지 전송용
         Long messageId,
         MessageType messageType,
         RoomType roomType,
         String roomId,
         Long receiver,
         Long sender,
         String message,
         Long serviceId,
         LocalDateTime createdAt,
         Long readMsgId,
         boolean isNewRoom
    ) {
        public static Chat from(Long messageId, ChatCommand.Chat command, boolean isNewRoom) {
            return Chat.builder()
                    .messageId(messageId)
                    .messageType(command.messageType())
                    .roomType(command.roomType())
                    .roomId(command.roomId())
                    .receiver(command.receiver())
                    .sender(command.sender())
                    .message(command.message())
                    .serviceId(command.serviceId())
                    .createdAt(command.createdAt())
                    .isNewRoom(isNewRoom)
                    .build();
        }

        public static Chat from(ChatCommand.Read command) {
            return Chat.builder()
                    .messageType(command.messageType())
                    .roomId(command.roomId())
                    .sender(command.sender())
                    .readMsgId(command.readMsgId())
                    .build();
        }
    }

    @Builder
    public record Save( // DB 저장용
        Long messageId,
        MessageType messageType,
        RoomType roomType,
        String roomId,
        Long receiver,
        Long sender,
        String message,
        Long serviceId,
        LocalDateTime createdAt
    ) {
        public static Save from(Long messageId, ChatCommand.Chat command) {
            return Save.builder()
                    .messageId(messageId)
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

    @Builder
    public record Join(
            MessageType messageType,
            RoomType roomType,
            String roomId,
            String roomName,
            Long sender,
            String message,
            LocalDateTime createdAt
    ) {
        public Sync toSyncEvent() {
            return Sync.builder()
                    .messageType(messageType)
                    .roomId(roomId)
                    .sender(sender)
                    .build();
        }
    }

    @Builder
    public record Sync(
            MessageType messageType,
            String roomId,
            Long sender
    ) {
    }
}
