package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.service.dto.ChatModel;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.global.exception.DataNotFoundException;
import teamssavice.ssavice.global.exception.ForbiddenException;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.service.dto.RoomQueryResult;
import teamssavice.ssavice.user.service.UserReadService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final UserReadService userReadService;
    private final RoomReadService roomReadService;
    private final ChatMemberReadService chatMemberReadService;

    @Transactional(readOnly = true)
    public Mono<List<ChatModel.Room>> findAllRooms(Auth auth) {

        return getMyMemberMap(auth.subject())
                .flatMap(myMemberMap -> {
                    List<String> roomIds = new ArrayList<>(myMemberMap.keySet());
                    if (myMemberMap.isEmpty()) return Mono.just(Collections.emptyList());

                    return getRoomsWithDependencies(roomIds)
                            .map(data -> assembleRooms(data, myMemberMap));
                });
    }

    private Mono<Map<String, ChatMemberEntity>> getMyMemberMap(Long subject) {
        return chatMemberReadService.findAllBySubject(subject)
                .collectMap(ChatMemberEntity::getRoomId);
    }

    private Mono<RoomQueryResult> getRoomsWithDependencies(List<String> roomIds) {

        return roomReadService.findAllByRoomIdIn(roomIds)
                .collectList()
                .flatMap(rooms ->
                        Mono.zip(
                            Mono.just(rooms),
                            chatMemberReadService.findAllByRoomIdIn(roomIds).collectMultimap(ChatMemberEntity::getRoomId)
                    ))
                .map(tuple -> new RoomQueryResult(tuple.getT1(), tuple.getT2()));
    }

    private List<ChatModel.Room> assembleRooms(
            RoomQueryResult data,
            Map<String, ChatMemberEntity> myMemberMap
    ) {
        return data.rooms().stream().map(room -> {
            Collection<ChatMemberEntity> members = data.memberMap().getOrDefault(room.getRoomId(), List.of());
            Long lastReadMsgId = myMemberMap.get(room.getRoomId()).getLastReadMsgId();

            return ChatModel.Room.of(room, members.size(), lastReadMsgId);
        }).toList();
    }

    @Transactional(readOnly = true)
    public Mono<ChatModel.Room> findByRoomId(String roomId, Auth auth) {
        return roomReadService.findByRoomId(roomId)
                .flatMap(room -> getOppositeName(room, auth));
    }

    private Mono<ChatModel.Room> getOppositeName(RoomEntity room, Auth auth) {
        if(RoomType.GROUP.equals(room.getType())) return Mono.just(ChatModel.Room.from(room, room.getRoomName()));

        String roomName = room.getRoomName();
        String[] parts = roomName.split("_");
        if(parts.length != 2) return Mono.error(new DataNotFoundException(ErrorCode.ROOM_NOT_FOUND));

        long id1 = Long.parseLong(parts[0]);
        long id2 = Long.parseLong(parts[1]);

        long myId = auth.subject();

        long oppSubject;
        if (myId == id1) oppSubject = id2;
        else if (myId == id2) oppSubject = id1;
        else return Mono.error(new ForbiddenException(ErrorCode.FORBIDDEN));

        return userReadService.findById(oppSubject)
                .switchIfEmpty(Mono.error(new DataNotFoundException(ErrorCode.ROOM_NOT_FOUND)))
                .map(user -> ChatModel.Room.from(room, user.getName()));
    }
}
