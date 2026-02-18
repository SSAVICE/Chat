package teamssavice.chat.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import teamssavice.chat.model.RoomEntity;

public interface RoomRepository extends R2dbcRepository<RoomEntity, Long> {
    Mono<RoomEntity> findByRoomId(String roomId);

    Mono<Boolean> existsByRoomId(String roomId);
}
