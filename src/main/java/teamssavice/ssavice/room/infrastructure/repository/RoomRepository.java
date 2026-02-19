package teamssavice.ssavice.room.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.room.entity.RoomEntity;

public interface RoomRepository extends R2dbcRepository<RoomEntity, Long> {
    Mono<RoomEntity> findByRoomId(String roomId);

    Mono<Boolean> existsByRoomId(String roomId);
}
