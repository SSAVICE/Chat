package teamssavice.ssavice.chat.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;

import java.util.List;

public interface ChatMessageRepository extends R2dbcRepository<ChatMessageEntity, Long> {

    Flux<ChatMessageEntity> findAllByMessageIdIn(List<Long> messageIds);
}
