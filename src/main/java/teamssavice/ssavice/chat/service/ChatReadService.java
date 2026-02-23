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
}
