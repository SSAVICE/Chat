package teamssavice.ssavice.fixture;

import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;

import java.time.LocalDateTime;

public class ChatMemberFixture {

    public static ChatMemberEntity chatMember(String roomId, Long subject, Long lastReadMsgId) {
        return ChatMemberEntity.builder()
                .roomId(roomId)
                .subject(subject)
                .joinedAt(LocalDateTime.now())
                .lastReadMsgId(lastReadMsgId)
                .build();
    }
}
