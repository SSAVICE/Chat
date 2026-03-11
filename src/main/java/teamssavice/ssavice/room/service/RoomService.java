package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.account.service.AccountReadService;
import teamssavice.ssavice.chat.service.dto.ChatCommand;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.chatmember.service.ChatMemberWriteService;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.global.exception.ForbiddenException;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.room.service.dto.RoomModel;
import teamssavice.ssavice.room.service.dto.RoomQueryResult;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    private final ChatMemberReadService chatMemberReadService;
    private final ChatMemberWriteService chatMemberWriteService;
    private final RoomReadService roomReadService;
    private final RoomWriteService roomWriteService;
    private final AccountReadService accountReadService;

    public Mono<List<RoomModel.Room>> findAllRooms(Auth auth) {

        return getMyMemberMap(auth.subject())
                .flatMap(myMemberMap -> {
                    List<String> roomIds = new ArrayList<>(myMemberMap.keySet());
                    if (myMemberMap.isEmpty()) return Mono.just(Collections.emptyList());

                    return getRoomsWithDependencies(roomIds, auth.subject())
                            .map(data -> assembleRooms(data, myMemberMap));
                });
    }

    private Mono<Map<String, ChatMemberEntity>> getMyMemberMap(Long subject) {
        return chatMemberReadService.findAllBySubject(subject)
                .collectMap(ChatMemberEntity::getRoomId);
    }

    private Mono<RoomQueryResult> getRoomsWithDependencies(List<String> roomIds, Long mySubject) {

        return roomReadService.findAllByRoomIdIn(roomIds)
                .collectList()
                .flatMap(rooms ->
                        Mono.zip(
                            Mono.just(rooms),
                            chatMemberReadService.findAllByRoomIdIn(roomIds).collectMultimap(ChatMemberEntity::getRoomId),
                                accountReadService.getNameMap(rooms, mySubject)
                    ))
                .map(tuple -> new RoomQueryResult(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private List<RoomModel.Room> assembleRooms(
            RoomQueryResult data,
            Map<String, ChatMemberEntity> myMemberMap
    ) {
        return data.rooms().stream().map(room -> {
            Collection<ChatMemberEntity> members = data.memberMap().getOrDefault(room.getRoomId(), List.of());
            Long lastReadMsgId = myMemberMap.get(room.getRoomId()).getLastReadMsgId();

            String roomName = RoomType.DM.equals(room.getType()) ? data.nameMap().get(room.getRoomName()) : room.getRoomName();
            return RoomModel.Room.of(room, members.size(), lastReadMsgId, roomName);
        }).toList();
    }

    public Mono<RoomModel.Detail> getRoomDetail(String roomId, Auth auth) {

        Mono<RoomEntity> roomMono = roomReadService.findByRoomId(roomId);
        Mono<List<ChatMemberEntity>> membersMono = chatMemberReadService.findAllByRoomId(roomId).collectList();
        Mono<String> oppNameMono = accountReadService.getOppName(roomId, auth.subject());

        return Mono.zip(roomMono, membersMono, oppNameMono)
                .flatMap(tuple -> {
                    RoomEntity room = tuple.getT1();
                    List<ChatMemberEntity> members = tuple.getT2();
                    String roomName = RoomType.DM.equals(room.getType()) ? tuple.getT3() : room.getRoomName();

                    return verifyMember(members, auth.subject())
                            .then(Mono.just(RoomModel.Detail.of(room, members, roomName)));
                });
    }

    public Mono<Boolean> createDMRoomIfNotExist(ChatCommand.Chat command) {
        if(!RoomType.DM.equals(command.roomType())) return Mono.just(false);
        String roomName = command.sender() + "_" + command.receiver();

        return createRoomIfNotExist(command.roomId(), roomName, command.roomType(), List.of(command.sender(), command.receiver()));
    }

    public Mono<Boolean> createRoomIfNotExist(String roomId, String roomName, RoomType roomType, List<Long> subjects) {

        return roomReadService.existsByRoomId(roomId)
                .flatMap(exist -> {
                    if (exist) return Mono.just(false);
                    return roomWriteService.save(roomId, roomName, roomType)
                            .then(chatMemberWriteService.saveAll(subjects, roomId))
                            .thenReturn(true);
                }).doOnError(e -> log.error("방 생성 실패: {}", e.getMessage(), e));
    }

    private Mono<Void> verifyMember(List<ChatMemberEntity> members, Long subject) {
        boolean isMember = members.stream().anyMatch(m -> m.getSubject().equals(subject));
        return isMember ? Mono.empty() : Mono.error(new ForbiddenException(ErrorCode.FORBIDDEN));
    }


}
