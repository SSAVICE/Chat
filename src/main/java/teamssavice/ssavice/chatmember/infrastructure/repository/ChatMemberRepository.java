package teamssavice.ssavice.chatmember.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;

public interface ChatMemberRepository extends R2dbcRepository<ChatMemberEntity, Long> {

    Flux<ChatMemberEntity> findAllBySubject(String subject);

    Flux<ChatMemberEntity> findAllByRoomId(String roomId);
}
