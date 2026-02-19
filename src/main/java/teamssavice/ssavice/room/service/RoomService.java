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
import teamssavice.ssavice.room.constants.RoomType;
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
                .flatMap(roomReadService::findByRoomId)
                .map(ChatModel.Room::from)
                .collectList();
    }

    public Mono<ChatModel.Room> findByRoomId(String roomId, Auth auth) {
        return roomReadService.findByRoomId(roomId)
                .flatMap(room -> getOppositeName(room, auth));
    }

    private Mono<ChatModel.Room> getOppositeName(RoomEntity room, Auth auth) {
        if(!RoomType.DM.equals(room.getType())) return Mono.just(ChatModel.Room.from(room));

        String[] arr = room.getRoomName().split("_");
        String oppSubject = auth.subject().equals(arr[0]) ? arr[1] : arr[0];
        Auth oppAuth = Auth.of(oppSubject);

        if(oppAuth.canAccess(Role.USER)) {
            return userReadService.findById(oppAuth.id())
                    .map(user -> ChatModel.Room.from(room, user.getName()));
        }

        return companyReadService.findById(oppAuth.id())
                .map(company -> ChatModel.Room.from(room, company.getCompanyName()));
    }
}
