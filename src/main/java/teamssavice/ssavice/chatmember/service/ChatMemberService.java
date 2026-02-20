package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.service.dto.ChatCommand;

@Service
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberWriteService chatMemberWriteService;

    public Mono<Void> updateLastReadMsgId(ChatCommand.Read command) {
        if(command.lastReadMsgId() == null) return Mono.empty();

        return chatMemberWriteService.updateLastReadMsgIdIfGreater(command.roomId(), command.sender(), command.lastReadMsgId());
    }
}
