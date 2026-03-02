package teamssavice.ssavice.chatmember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    @Transactional
    public Mono<Void> updateLastReadMsgIdIfGreater(ChatCommand.Read command) {

        return chatMemberRepository.updateLastReadMsgIdIfGreater(command.roomId(), command.sender(), command.readMsgId())
                .doOnError(e -> log.error("마지막 메시지 기록 저장 실패: {}", e.getMessage(), e))
                .then();
    }

    @Transactional
    public Mono<Void> deleteByRoomIdAndSender(String roomId, Long sender) {
        return chatMemberRepository.deleteByRoomIdAndSubject(roomId, sender)
                .doOnError(e -> log.error("roomId와 Subject로 방 나가기 실패: {}", e.getMessage(), e))
                .then();
    }
}
