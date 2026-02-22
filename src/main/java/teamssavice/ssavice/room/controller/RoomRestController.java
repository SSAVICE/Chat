package teamssavice.ssavice.room.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.annotation.CurrentAuth;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.room.controller.dto.ChatResponse;
import teamssavice.ssavice.room.service.RoomService;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomRestController {

    private final RoomService roomService;

    // 방 목록 반환
    @GetMapping("/list")
    public Mono<ResponseEntity<ChatResponse.Rooms>> getRooms(
            @CurrentAuth Auth auth
    ) {
        return roomService.findAllRooms(auth)
                .map(ChatResponse.Rooms::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 방 정보 반환
    @GetMapping("/{room-id}")
    public Mono<ResponseEntity<ChatResponse.Room>> getRoomByRoomId(
            @PathVariable("room-id") @NotBlank String roomId,
            @CurrentAuth Auth auth
    ) {
        return roomService.findByRoomId(roomId, auth)
                .map(ChatResponse.Room::from)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
