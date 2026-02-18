package teamssavice.chat.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import teamssavice.chat.model.ChatMemberEntity;

public interface ChatMemberRepository extends R2dbcRepository<ChatMemberEntity, Long> {

    Flux<ChatMemberEntity> findAllByUserId(String userId);

    Flux<ChatMemberEntity> findAllByRoomId(String roomId);
}
