package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberWriteService chatMemberWriteService;

    public Mono<Void> leaveRoom(String roomId, Long subject) {
        return chatMemberWriteService.deleteByRoomIdAndSender(roomId, subject);
    }
}
