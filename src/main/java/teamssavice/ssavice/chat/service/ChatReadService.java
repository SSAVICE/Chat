package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.chat.infrastructure.repository.ChatMessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatReadService {
    private final ChatMessageRepository chatMessageRepository;

    public Flux<ChatMessageEntity> findAllByMessageIdIn(List<Long> messageIds) {
        return chatMessageRepository.findAllByMessageIdIn(messageIds);
    }

    public Flux<ChatMessageEntity> findMessagesAfterCursor(String roomId, Long cursor, int limit) {
        return chatMessageRepository.findMessagesAfterCursor(roomId, cursor, limit);
    }

    public Flux<ChatMessageEntity> findMessagesBeforeCursor(String roomId, Long cursor, int limit) {
        return chatMessageRepository.findMessagesBeforeCursor(roomId, cursor, limit);
    }

    public Flux<ChatMessageEntity> findLatestMessages(String roomId, int limit) {
        return chatMessageRepository.findLatestMessages(roomId, limit);
    }
}
