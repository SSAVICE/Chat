package teamssavice.ssavice.room.infrastructure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import teamssavice.ssavice.chat.ChatMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomChannel {
    private final Sinks.Many<ChatMessage> sink;
    private final Flux<ChatMessage> flux;
    private final AtomicInteger subscriberCount = new AtomicInteger(0);

    public RoomChannel(String roomId, Map<String, RoomChannel> rooms) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.flux = sink.asFlux()
                .doOnSubscribe(sub -> subscriberCount.incrementAndGet())
                .doFinally(sig -> {
                    if (subscriberCount.decrementAndGet() == 0) {
                        rooms.remove(roomId);
                    }
                });
    }

    public void emit(ChatMessage message) {
        sink.tryEmitNext(message);
    }

    public Flux<ChatMessage> getFlux() {
        return flux;
    }
}
