package teamssavice.ssavice.chat.infrastructure.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;

import java.util.List;

public interface ChatMessageRepository extends R2dbcRepository<ChatMessageEntity, Long> {

    Flux<ChatMessageEntity> findAllByMessageIdIn(List<Long> messageIds);

    @Query("SELECT * FROM chat_message " +
            "WHERE room_id = :roomId " +
            "AND message_id > :cursor " +
            "ORDER BY message_id ASC " +
            "LIMIT :limit")
    Flux<ChatMessageEntity> findMessagesAfterCursor(  // cursor 이후 메시지(아래로 스크롤) (room_id, message_id) 인덱스 필요
            @Param("roomId") String roomId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    @Query("SELECT * FROM chat_message " +
            "WHERE room_id = :roomId " +
            "AND message_id < :cursor " +
            "ORDER BY message_id DESC " +
            "LIMIT :limit")
    Flux<ChatMessageEntity> findMessagesBeforeCursor(  // cursor 이전 메시지(위로 스크롤)
           @Param("roomId") String roomId,
           @Param("cursor") Long cursor,
           @Param("limit") int limit
    );

    @Query("SELECT * FROM chat_message " +
            "WHERE room_id = :roomId " +
            "ORDER BY message_id DESC " +
            "LIMIT :limit")
    Flux<ChatMessageEntity> findLatestMessages(  // 가장 최신 메시지
           @Param("roomId") String roomId,
           @Param("limit") int limit
    );
}
