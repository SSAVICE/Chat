package teamssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.chat.model.MessageType;
import teamssavice.chat.model.RoomType;
import teamssavice.chat.websocket.dto.WebSocketRequest;

import java.time.LocalDateTime;

public class ChatCommand {

    @Builder
    public record Chat(
            MessageType type,
            RoomType roomType,
            String roomId,
            String receiver,
            String sender,
            String message,
            LocalDateTime createdAt
    ) {
        public static Chat from(WebSocketRequest.Chat request) {
            return ChatCommand.Chat.builder()
                    .type(request.type())
                    .roomType(request.roomType())
                    .roomId(RoomType.DM.equals(request.roomType())
                        ? generateDMRoomId(request.sender(), request.receiver())
                        : request.roomId()
                    ).receiver(request.receiver())
                    .sender(request.sender())
                    .message(request.message())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        public static String generateDMRoomId(String sender, String receiver) {
            return sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;
        }
    }
}
