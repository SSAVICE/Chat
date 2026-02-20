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
        public static Chat from(WebSocketRequest request, String sender) {
            return Chat.builder()
                    .messageType(request.getMessageType())
                    .roomType(request.getRoomType())
                    .roomId(RoomType.DM.equals(request.getRoomType()) && request.getRoomId().isEmpty()
                        ? generateDMRoomId(sender, request.getReceiver())
                        : request.getRoomId()
                    ).receiver(request.getReceiver())
                    .sender(sender)
                    .message(request.getMessage())
                    .serviceId(request.getServiceId())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        public static String generateDMRoomId(String sender, String receiver) {
            return sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;
        }
    }

    @Builder
    public record Read(
            MessageType messageType,
            String roomId,
            String sender,
            Long lastReadMsgId
    ) {
        public static Read from(WebSocketRequest request, String sender) {
            return Read.builder()
                    .messageType(request.getMessageType())
                    .roomId(request.getRoomId())
                    .sender(sender)
                    .lastReadMsgId(request.getLastReadMsgId())
                    .build();
        }
    }
}
