package teamssavice.ssavice.room.infrastructure.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.util.List;

public interface RoomRepository extends R2dbcRepository<RoomEntity, Long> {
    Mono<RoomEntity> findByRoomId(String roomId);

    Mono<Boolean> existsByRoomId(String roomId);

    @Modifying
    @Query("UPDATE room SET last_msg_id = :lastMsgId " +
            "WHERE room_id = :roomId " +
            "AND last_msg_id < :lastMsgId")
    Mono<Integer> updateLastMsgId(String roomId, Long lastMsgId);

    Flux<RoomEntity> findAllByRoomIdIn(List<String> roomIds);
}
