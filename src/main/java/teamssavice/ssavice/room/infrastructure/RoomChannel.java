package teamssavice.ssavice.room.infrastructure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import teamssavice.ssavice.chat.ChatMessage;

import java.util.Map;

public class RoomChannel {
    private final Sinks.Many<ChatMessage> sink;
    private final Flux<ChatMessage> flux;

    public RoomChannel(String roomId, Map<String, RoomChannel> rooms) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.flux = sink.asFlux()
                .doFinally(sig -> {
                    if (sink.currentSubscriberCount() == 0) {
                        rooms.remove(roomId);
                    }
                })
                .publish()
                .refCount(1);  // 마지막 사용자가 나가면 방 삭제
    }

    public void emit(ChatMessage message) {
        sink.tryEmitNext(message);
    }

    public Flux<ChatMessage> getFlux() {
        return flux;
    }
}
