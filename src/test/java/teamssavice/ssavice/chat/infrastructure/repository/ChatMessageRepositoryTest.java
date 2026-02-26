package teamssavice.ssavice.chat.infrastructure.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.fixture.ChatMessageFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final String roomId1 = "1_2";
    private final String roomId2 = "1_3";
    private final Long subject = 1L;
    private final String[] messages1 = {"hello", "world", "this", "is", "last message"};
    private final String[] messages2 = {"hello2", "world2", "this2", "is2", "last message2"};

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll().block();

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.chatDmMessage(1L, roomId1, messages1[0], subject),
                ChatMessageFixture.chatDmMessage(2L, roomId1, messages1[1], subject),
                ChatMessageFixture.chatDmMessage(3L, roomId1, messages1[2], subject),
                ChatMessageFixture.chatDmMessage(4L, roomId1, messages1[3], subject),
                ChatMessageFixture.chatDmMessage(5L, roomId1, messages1[4], subject),

                ChatMessageFixture.chatDmMessage(1L, roomId2, messages2[0], subject),
                ChatMessageFixture.chatDmMessage(2L, roomId2, messages2[1], subject),
                ChatMessageFixture.chatDmMessage(3L, roomId2, messages2[2], subject),
                ChatMessageFixture.chatDmMessage(4L, roomId2, messages2[3], subject),
                ChatMessageFixture.chatDmMessage(5L, roomId2, messages2[4], subject)
        )).collectList().block();
    }

    @Test
    @DisplayName("아래로 스크롤 테스트(cursor 이후 메시지)")
    void findMessagesAfterCursorTest() {

        StepVerifier.create(chatMessageRepository.findMessagesAfterCursor(roomId1, 2L, 2).collectList())
                .assertNext(entities -> {
                    assertThat(entities).hasSize(2);

                    ChatMessageEntity entity1 = entities.get(0);
                    assertThat(entity1.getMessageId()).isEqualTo(3L);
                    assertThat(entity1.getMessage()).isEqualTo(messages1[2]);

                    ChatMessageEntity entity2 = entities.get(1);
                    assertThat(entity2.getMessageId()).isEqualTo(4L);
                    assertThat(entity2.getMessage()).isEqualTo(messages1[3]);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("위로 스크롤 테스트(cursor 이전 메시지)")
    void findMessagesBeforeCursorTest() {

        StepVerifier.create(chatMessageRepository.findMessagesBeforeCursor(roomId1, 4L, 2).collectList())
                .assertNext(entities -> {
                    assertThat(entities).hasSize(2);

                    ChatMessageEntity entity1 = entities.get(0);
                    assertThat(entity1.getMessageId()).isEqualTo(3L);
                    assertThat(entity1.getMessage()).isEqualTo(messages1[2]);

                    ChatMessageEntity entity2 = entities.get(1);
                    assertThat(entity2.getMessageId()).isEqualTo(2L);
                    assertThat(entity2.getMessage()).isEqualTo(messages1[1]);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("최신 메시지 테스트 테스트")
    void findLatestMessagesTest() {

        StepVerifier.create(chatMessageRepository.findLatestMessages(roomId2, 2).collectList())
                .assertNext(entities -> {
                    assertThat(entities).hasSize(2);

                    ChatMessageEntity entity1 = entities.get(0);
                    assertThat(entity1.getMessageId()).isEqualTo(5L);
                    assertThat(entity1.getMessage()).isEqualTo(messages2[4]);

                    ChatMessageEntity entity2 = entities.get(1);
                    assertThat(entity2.getMessageId()).isEqualTo(4L);
                    assertThat(entity2.getMessage()).isEqualTo(messages2[3]);
                })
                .verifyComplete();
    }
}