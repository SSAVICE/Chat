package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberWriteService chatMemberWriteService;

    public Mono<Void> leaveRoom(String roomId, Long subject) {
        return chatMemberWriteService.deleteByRoomIdAndSender(roomId, subject);
    }

    public Mono<Void> joinRoom(String roomId, Long subject, LocalDateTime createdAt) {
        return chatMemberWriteService.save(subject, roomId, createdAt);
    }
}
