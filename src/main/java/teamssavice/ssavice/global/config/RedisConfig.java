package teamssavice.ssavice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory
    ) {
        RedisSerializationContext<String, Long> context =
                RedisSerializationContext
                        .<String, Long>newSerializationContext(new StringRedisSerializer())
                        .value(new GenericToStringSerializer<>(Long.class))
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
