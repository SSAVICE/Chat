package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemberWriteService {
    private final ChatMemberRepository chatMemberRepository;

    public Mono<Void> saveAll(List<String> subjects, String roomId, LocalDateTime createdAt) {
        List<ChatMemberEntity> members = subjects.stream()
                .map(subject -> ChatMemberEntity.builder()
                        .roomId(roomId)
                        .subject(subject)
                        .joinedAt(createdAt)
                        .lastReadMsgId(0L)
                        .build())
                .toList();

        return chatMemberRepository.saveAll(members)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                .then();
    }
}
