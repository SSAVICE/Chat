package teamssavice.ssavice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageIdGenerator {

    private final ReactiveRedisTemplate<String, Long> redisTemplate;

    public Mono<Long> nextMessageId(String roomId) {
        String key = "room:" + roomId + ":seq";
        return redisTemplate.opsForValue().increment(key);
    }
}
