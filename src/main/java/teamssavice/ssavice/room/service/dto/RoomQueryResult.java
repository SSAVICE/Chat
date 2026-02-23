package teamssavice.ssavice.room.service.dto;

import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record RoomQueryResult(
        List<RoomEntity> rooms,
        Map<String, Collection<ChatMemberEntity>> memberMap,
        Map<String, ChatMessageEntity> lastMessageMap
) {}