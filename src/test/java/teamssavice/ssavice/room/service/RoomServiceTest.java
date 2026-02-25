package teamssavice.ssavice.room.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import teamssavice.ssavice.chat.infrastructure.repository.ChatMessageRepository;
import teamssavice.ssavice.chatmember.infrastructure.repository.ChatMemberRepository;
import teamssavice.ssavice.fixture.ChatMemberFixture;
import teamssavice.ssavice.fixture.ChatMessageFixture;
import teamssavice.ssavice.fixture.RoomFixture;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.infrastructure.repository.RoomRepository;
import teamssavice.ssavice.room.service.dto.RoomModel;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RoomServiceTest {
    @Autowired
    private RoomService roomService;
    @Autowired
    private ChatMemberRepository chatMemberRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final String roomId1 = "1_2";
    private final String roomId2 = "1_3";
    private final Long subject = 1L;
    private final LocalDateTime now = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000) * 1_000);


    @BeforeEach
    void setUp() {
        RoomEntity room1 = RoomFixture.room(roomId1, "1_2", 3L, now.plusMinutes(4));
        RoomEntity room2 = RoomFixture.room(roomId2, "1_3", 2L, now.plusMinutes(3));
        roomRepository.save(room1).block();
        roomRepository.save(room2).block();

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.chatDmMessage(1L, roomId1, "hello", subject, now),
                ChatMessageFixture.chatDmMessage(1L, roomId2, "hello", subject, now.plusMinutes(1)),
                ChatMessageFixture.chatDmMessage(2L, roomId1, "world", subject, now.plusMinutes(2)),
                ChatMessageFixture.chatDmMessage(2L, roomId2, "world", subject, now.plusMinutes(3)),
                ChatMessageFixture.chatDmMessage(3L, roomId1, "last message", subject, now.plusMinutes(4))
        )).collectList().block();

        chatMemberRepository.save(ChatMemberFixture.chatMember(roomId1, subject, 1L)).block();
        chatMemberRepository.save(ChatMemberFixture.chatMember(roomId2, subject, 2L)).block();
    }


    @Test
    @DisplayName("Room 목록 조회 테스트(roomId, roomName, roomType, serviceId, 안읽은 채팅 수, 마지막 채팅 메시지, 참여자수 반환)")
    void findAllRoomsTest() {
        Auth auth = new Auth(subject);

        StepVerifier.create(roomService.findAllRooms(auth))
                .assertNext(rooms -> {
                    assertThat(rooms).hasSize(2);

                    RoomModel.Room model1 = rooms.get(0);
                    assertThat(model1.roomId()).isEqualTo(roomId1);
                    assertThat(model1.lastMsgId()).isEqualTo(3L);
                    assertThat(model1.lastMsgAt()).isEqualTo(now.plusMinutes(4));
                    assertThat(model1.unReadMsgCnt()).isEqualTo(2);
                    assertThat(model1.memberCnt()).isEqualTo(1);

                    RoomModel.Room model2 = rooms.get(1);
                    assertThat(model2.roomId()).isEqualTo(roomId2);
                    assertThat(model2.lastMsgId()).isEqualTo(2L);
                    assertThat(model2.lastMsgAt()).isEqualTo(now.plusMinutes(3));
                    assertThat(model2.unReadMsgCnt()).isEqualTo(0);
                    assertThat(model2.memberCnt()).isEqualTo(1);
                })
                .verifyComplete();
    }
}