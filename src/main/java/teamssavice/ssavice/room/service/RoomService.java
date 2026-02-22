package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import teamssavice.ssavice.user.service.UserReadService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final UserReadService userReadService;
    private final RoomReadService roomReadService;
    private final ChatMemberReadService chatMemberReadService;

    public Mono<List<ChatModel.Room>> findAllRooms(Auth auth) {
        return chatMemberReadService.findAllBySubject(auth.subject())
                .map(ChatMemberEntity::getRoomId)
                .flatMap(roomId -> findByRoomId(roomId, auth))
                .collectList();
    }

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
