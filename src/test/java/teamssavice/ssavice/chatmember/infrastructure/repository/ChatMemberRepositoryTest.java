package teamssavice.ssavice.chatmember.infrastructure.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.fixture.ChatMemberFixture;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class ChatMemberRepositoryTest {

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    private final String roomId = "1_2";
    private final Long subject = 1L;

    @BeforeEach
    void setUp() {
        chatMemberRepository.deleteAll().block();

        ChatMemberEntity memberEntity = ChatMemberFixture.chatMember(roomId, subject, 10L);
        chatMemberRepository.save(memberEntity).block();
    }

    @Test
    @DisplayName("lastReadMsgId가 DB에 저장된 값보다 클 때 업데이트 테스트")
    void updateLastReadMsgIdSuccessTest() {
        Long lastReadMsgId = 11L;

        StepVerifier.create( chatMemberRepository.updateLastReadMsgIdIfGreater(roomId, subject, lastReadMsgId))
                .expectNext(1)
                .verifyComplete();

        StepVerifier.create(chatMemberRepository.findByRoomIdAndSubject(roomId, subject))
                .assertNext(updated -> assertThat(lastReadMsgId).isEqualTo(updated.getLastReadMsgId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("lastReadMsgId가 DB에 저장된 값보다 작으면 무시 테스트")
    void updateLastReadMsgIdIgnoreTest() {
        String roomId = "1_2";
        Long subject = 1L;
        Long lastReadMsgId = 9L;
        Long originalReadMsgId = 10L;

        // when
        Mono<Integer> result = chatMemberRepository.updateLastReadMsgIdIfGreater(roomId, subject, lastReadMsgId);

        // then
        StepVerifier.create(chatMemberRepository.updateLastReadMsgIdIfGreater(roomId, subject, lastReadMsgId))
                .expectNext(0)
                .verifyComplete();

        StepVerifier.create(chatMemberRepository.findByRoomIdAndSubject(roomId, subject))
                .assertNext(updated -> assertThat(originalReadMsgId).isEqualTo(updated.getLastReadMsgId()))
                .verifyComplete();
    }
}