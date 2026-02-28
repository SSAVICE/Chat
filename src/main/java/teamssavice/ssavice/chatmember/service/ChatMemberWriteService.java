package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemberWriteService {
    private final ChatMemberRepository chatMemberRepository;

    public Mono<Void> saveAll(List<Long> subjects, String roomId) {
        LocalDateTime now = LocalDateTime.now();
        List<ChatMemberEntity> members = subjects.stream()
                .map(subject -> ChatMemberEntity.builder()
                        .roomId(roomId)
                        .subject(subject)
                        .joinedAt(now)
                        .lastReadMsgId(0L)
                        .build())
                .toList();

        return chatMemberRepository.saveAll(members)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public Mono<Void> save(Long subject, String roomId, LocalDateTime createdAt) {
        ChatMemberEntity entity = ChatMemberEntity.builder()
                .roomId(roomId)
                .subject(subject)
                .joinedAt(createdAt)
                .lastReadMsgId(0L)
                .build();

        return chatMemberRepository.save(entity)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }

    public Mono<Void> updateLastReadMsgIdIfGreater(ChatCommand.Read command) {

        return chatMemberRepository.updateLastReadMsgIdIfGreater(command.roomId(), command.sender(), command.readMsgId())
                .doOnError(e -> System.out.println("마지막 메시지 기록 저장 실패: " + e.getMessage()))
                .then();
    }
}
