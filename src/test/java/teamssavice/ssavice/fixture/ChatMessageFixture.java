package teamssavice.ssavice.fixture;

import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

public class ChatMessageFixture {

    public static ChatMessageEntity chatDmMessage(Long messageId, String roomId, String message, Long sender, LocalDateTime createdAt) {
        return ChatMessageEntity.builder()
                .messageId(messageId)
                .roomType(RoomType.DM)
                .messageType(MessageType.TEXT)
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .createdAt(createdAt)
                .build();
    }

    public static ChatMessageEntity chatDmMessage(Long messageId, String roomId, String message, Long sender) {
        return ChatMessageEntity.builder()
                .messageId(messageId)
                .roomType(RoomType.DM)
                .messageType(MessageType.TEXT)
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
