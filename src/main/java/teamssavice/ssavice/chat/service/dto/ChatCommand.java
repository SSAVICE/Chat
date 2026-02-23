package teamssavice.ssavice.chat.service.dto;

import lombok.Builder;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.chat.websocket.dto.WebSocketRequest;

import java.time.LocalDateTime;

public class ChatCommand {

    @Builder
    public record Chat(
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
        public static Chat from(WebSocketRequest request, Long sender) {
            return Chat.builder()
                    .messageType(request.getMessageType())
                    .roomType(request.getRoomType())
                    .roomId(RoomType.DM.equals(request.getRoomType())
                        ? generateDMRoomId(sender, request.getReceiver())
                        : request.getRoomId()
                    ).receiver(request.getReceiver())
                    .sender(sender)
                    .message(request.getMessage())
                    .serviceId(request.getServiceId())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        public static String generateDMRoomId(Long sender, Long receiver) {
            return sender.compareTo(receiver) < 0 ? sender + "_" + receiver : receiver + "_" + sender;
        }
    }

    @Builder
    public record Read(
            MessageType messageType,
            String roomId,
            Long sender,
            Long[] readMsgIds
    ) {
        public static Read from(WebSocketRequest request, Long sender) {
            return Read.builder()
                    .messageType(request.getMessageType())
                    .roomId(request.getRoomId())
                    .sender(sender)
                    .readMsgIds(request.getReadMsgIds())
                    .build();
        }
    }
}
