package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.global.exception.ForbiddenException;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.service.dto.RoomModel;
import teamssavice.ssavice.room.service.dto.RoomQueryResult;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomReadService roomReadService;
    private final ChatMemberReadService chatMemberReadService;

    @Transactional(readOnly = true)
    public Mono<List<RoomModel.Room>> findAllRooms(Auth auth) {

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

    private List<RoomModel.Room> assembleRooms(
            RoomQueryResult data,
            Map<String, ChatMemberEntity> myMemberMap
    ) {
        return data.rooms().stream().map(room -> {
            Collection<ChatMemberEntity> members = data.memberMap().getOrDefault(room.getRoomId(), List.of());
            Long lastReadMsgId = myMemberMap.get(room.getRoomId()).getLastReadMsgId();

            return RoomModel.Room.of(room, members.size(), lastReadMsgId);
        }).toList();
    }

    @Transactional(readOnly = true)
    public Mono<RoomModel.Detail> getRoomDetail(String roomId, Auth auth) {

        Mono<RoomEntity> roomMono = roomReadService.findByRoomId(roomId);
        Mono<List<ChatMemberEntity>> membersMono = chatMemberReadService.findAllByRoomId(roomId).collectList();

        return Mono.zip(roomMono, membersMono)
                .flatMap(tuple -> {
                    RoomEntity room = tuple.getT1();
                    List<ChatMemberEntity> members = tuple.getT2();

                    return verifyMember(members, auth.subject())
                            .then(Mono.just(RoomModel.Detail.of(room, members)));
                });
    }

    private Mono<Void> verifyMember(List<ChatMemberEntity> members, Long subject) {
        boolean isMember = members.stream().anyMatch(m -> m.getSubject().equals(subject));
        return isMember ? Mono.empty() : Mono.error(new ForbiddenException(ErrorCode.FORBIDDEN));
    }

}
