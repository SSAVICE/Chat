package teamssavice.ssavice.chatmember.infrastructure.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;

import java.util.List;

public interface ChatMemberRepository extends R2dbcRepository<ChatMemberEntity, Long> {

    Flux<ChatMemberEntity> findAllBySubject(Long subject);

    Flux<ChatMemberEntity> findAllByRoomId(String roomId);

    @Modifying
    @Query("UPDATE chat_member SET last_read_msg_id = :lastReadMsgId " +
            "WHERE room_id = :roomId " +
            "AND subject = :sender " +
            "AND last_read_msg_id < :lastReadMsgId")
    Mono<Integer> updateLastReadMsgIdIfGreater(String roomId, Long sender, Long lastReadMsgId);

    Mono<ChatMemberEntity> findByRoomIdAndSubject(String roomId, Long subject);

    Flux<ChatMemberEntity> findAllByRoomIdIn(List<String> roomIds);

    Mono<Boolean> existsByRoomIdAndSubject(String roomId, Long subject);
}
