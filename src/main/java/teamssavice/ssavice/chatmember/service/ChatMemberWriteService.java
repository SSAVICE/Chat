package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemberWriteService {
    private final ChatMemberRepository chatMemberRepository;

    public Flux<ChatMemberEntity> saveAll(List<String> subjects, String roomId, LocalDateTime createdAt) {
        List<ChatMemberEntity> members = subjects.stream()
                .map(subject -> ChatMemberEntity.builder()
                        .roomId(roomId)
                        .subject(subject)
                        .joinedAt(createdAt)
                        .isLeft(false)
                        .lastReadMsgId(0L)
                        .build())
                .toList();

        return chatMemberRepository.saveAll(members);
    }
}
