package teamssavice.ssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.chat.websocket.dto.WebSocketRequest;

import java.time.LocalDateTime;

public class ChatCommand {

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
        public static Chat from(WebSocketRequest.Chat request) {
            return Chat.builder()
                    .messageType(request.messageType())
                    .roomType(request.roomType())
                    .roomId(RoomType.DM.equals(request.roomType()) && request.roomId().isEmpty()
                        ? generateDMRoomId(request.sender(), request.receiver())
                        : request.roomId()
                    ).receiver(request.receiver())
                    .sender(request.sender())
                    .message(request.message())
                    .serviceId(request.serviceId())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        public static String generateDMRoomId(String sender, String receiver) {
            return sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;
        }
    }
}
