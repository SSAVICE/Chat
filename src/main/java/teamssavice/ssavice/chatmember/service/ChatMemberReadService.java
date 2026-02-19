package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.DataNotFoundException;

@Service
@RequiredArgsConstructor
public class ChatMemberReadService {
    private final ChatMemberRepository chatMemberRepository;

    public Flux<ChatMemberEntity> findAllBySubject(String subject) {
        return chatMemberRepository.findAllBySubject(subject);
    }

    public Flux<ChatMemberEntity> findAllByRoomId(String roomId) {
        return chatMemberRepository.findAllByRoomId(roomId)
                .switchIfEmpty(Mono.error(new DataNotFoundException(ErrorCode.ROOM_NOT_FOUND)));
    }
}
