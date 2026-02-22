package teamssavice.ssavice.chatmember.infrastructure.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ChatMemberRepositoryTest {

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Test
    @DisplayName("lastReadMsgId가 DB에 저장된 값보다 클 때 업데이트 테스트")
    void updateLastReadMsgIdSuccessTest() {
        // given
        String roomId = "1_2";
        Long subject = 1L;
        Long lastReadMsgId = 11L;
        ChatMemberEntity entity = ChatMemberEntity.builder()
                .roomId(roomId)
                .subject(subject)
                .joinedAt(LocalDateTime.now())
                .lastReadMsgId(10L)
                .build();
        StepVerifier.create(chatMemberRepository.save(entity))
                .expectNextCount(1)
                .verifyComplete();

        // when
        Mono<Integer> result = chatMemberRepository.updateLastReadMsgIdIfGreater(roomId, subject, lastReadMsgId);

        // then
        StepVerifier.create(result).expectNext(1).verifyComplete();

        StepVerifier.create(chatMemberRepository.findByRoomIdAndSubject(roomId, subject))
                .assertNext(updated -> assertThat(lastReadMsgId).isEqualTo(updated.getLastReadMsgId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("lastReadMsgId가 DB에 저장된 값보다 작으면 무시 테스트")
    void updateLastReadMsgIdIgnoreTest() {
        // given
        String roomId = "1_2";
        Long subject = 1L;
        Long lastReadMsgId = 9L;
        Long originalReadMsgId = 10L;
        ChatMemberEntity entity = ChatMemberEntity.builder()
                .roomId(roomId)
                .subject(subject)
                .joinedAt(LocalDateTime.now())
                .lastReadMsgId(originalReadMsgId)
                .build();
        StepVerifier.create(chatMemberRepository.save(entity))
                .expectNextCount(1)
                .verifyComplete();

        // when
        Mono<Integer> result = chatMemberRepository.updateLastReadMsgIdIfGreater(roomId, subject, lastReadMsgId);

        // then
        StepVerifier.create(result).expectNext(0).verifyComplete();

        StepVerifier.create(chatMemberRepository.findByRoomIdAndSubject(roomId, subject))
                .assertNext(updated -> assertThat(originalReadMsgId).isEqualTo(updated.getLastReadMsgId()))
                .verifyComplete();
    }
}