package teamssavice.ssavice.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.service.dto.ChatModel;
import teamssavice.ssavice.chatmember.entity.ChatMemberEntity;
import teamssavice.ssavice.chatmember.service.ChatMemberReadService;
import teamssavice.ssavice.company.service.CompanyReadService;
import teamssavice.ssavice.global.constants.Role;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;
import teamssavice.ssavice.user.service.UserReadService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final CompanyReadService companyReadService;
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

        String oppSubject = room.getOppSubject(auth.subject());
        Auth oppAuth = Auth.of(oppSubject);

        if(oppAuth.canAccess(Role.USER)) {
            return userReadService.findById(oppAuth.id())
                    .map(user -> ChatModel.Room.from(room, user.getName()));
        }

        return companyReadService.findById(oppAuth.id())
                .map(company -> ChatModel.Room.from(room, company.getCompanyName()));
    }
}
