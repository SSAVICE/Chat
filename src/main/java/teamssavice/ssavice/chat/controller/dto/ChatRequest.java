package teamssavice.ssavice.chat.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import teamssavice.ssavice.chat.CursorDirection;
import teamssavice.ssavice.chat.service.dto.ChatCommand;

public class ChatRequest {

    public record MessageCursor(
            @Positive Long cursor,
            CursorDirection direction,
            @NotBlank String roomId,
            @Positive Integer size
    ) {

        public CursorDirection directionOrDefault() {
            return direction == null ? CursorDirection.LATEST : direction;
        }

        public int sizeOrDefault() {
            return size == null ? 50 : size;
        }

        public ChatCommand.MessageCursor toCommand() {
            return ChatCommand.MessageCursor.builder()
                    .cursor(cursor)
                    .direction(directionOrDefault())
                    .roomId(roomId)
                    .size(sizeOrDefault())
                    .build();
        }
    }
}
