package com.routesearch.config;

import com.routesearch.model.PathResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures a {@link RedisTemplate} that stores {@link PathResult} values as
 * JSON under String keys, so cached routes are human-readable via redis-cli.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PathResult> pathResultRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, PathResult> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PathResult.class));
        template.afterPropertiesSet();
        return template;
    }
}
