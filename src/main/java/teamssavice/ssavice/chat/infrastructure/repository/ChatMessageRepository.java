package teamssavice.ssavice.chat.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;

public interface ChatMessageRepository extends R2dbcRepository<ChatMessageEntity, Long> {
}
