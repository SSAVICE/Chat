package teamssavice.chat.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import teamssavice.chat.model.Room;

public interface RoomRepository extends R2dbcRepository<Room, Long> {
    Mono<Room> findByRoomId(String roomId);
}
