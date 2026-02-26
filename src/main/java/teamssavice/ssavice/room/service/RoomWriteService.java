package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.infrastructure.repository.RoomRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoomWriteService {
    private final RoomRepository roomRepository;

    public Mono<RoomEntity> save(String roomId, String roomName, LocalDateTime createdAt) {
        RoomEntity entity = RoomEntity.builder()
                .roomId(roomId)
                .roomName(roomName)
                .type(RoomType.DM)
                .createdAt(createdAt)
                .build();

        return roomRepository.save(entity)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.empty());
    }

    public Mono<Void> updateLastMsgId(String roomId, Long lastMsgId, LocalDateTime lastMsgAt, String lastMsg) {

        return roomRepository.updateLastMsgId(roomId, lastMsgId, lastMsgAt, lastMsg)
                .doOnError(e -> System.out.println("Room의 lastMsgId 업데이트 실패: " + e.getMessage()))
                .then();
    }
}
