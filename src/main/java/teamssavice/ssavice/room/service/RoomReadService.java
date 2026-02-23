package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.DataNotFoundException;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.infrastructure.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomReadService {
    private final RoomRepository roomRepository;

    public Mono<RoomEntity> findByRoomId(String roomId) {
        return roomRepository.findByRoomId(roomId)
                .switchIfEmpty(Mono.error(new DataNotFoundException(ErrorCode.ROOM_NOT_FOUND)));
    }

    public Mono<Boolean> existsByRoomId(String roomId) {
        return roomRepository.existsByRoomId(roomId);
    }

    public Flux<RoomEntity> findAllByRoomIdIn(List<String> roomIds) {
        return roomRepository.findAllByRoomIdIn(roomIds);
    }
}
