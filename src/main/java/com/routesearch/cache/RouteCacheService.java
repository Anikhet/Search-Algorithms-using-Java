package com.routesearch.cache;

import com.routesearch.model.PathResult;
import com.routesearch.search.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Redis-backed cache for computed routes. Keys look like
 * {@code route:astar:seattle->boston}. All Redis access is defensive: if Redis
 * is unavailable the service degrades gracefully and the API still answers by
 * recomputing routes.
 */
@Service
public class RouteCacheService {

    private static final Logger log = LoggerFactory.getLogger(RouteCacheService.class);

    private final RedisTemplate<String, PathResult> redis;
    private final Duration ttl;

    public RouteCacheService(RedisTemplate<String, PathResult> redis,
                             @Value("${route.cache.ttl-seconds:3600}") long ttlSeconds) {
        this.redis = redis;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public Optional<PathResult> get(Algorithm algorithm, String from, String to) {
        String key = key(algorithm, from, to);
        try {
            PathResult cached = redis.opsForValue().get(key);
            if (cached != null) {
                cached.setCached(true);
                return Optional.of(cached);
            }
        } catch (RuntimeException e) {
            log.warn("Redis GET failed for {} ({}); serving uncached", key, e.getMessage());
        }
        return Optional.empty();
    }

    public void put(Algorithm algorithm, String from, String to, PathResult result) {
        String key = key(algorithm, from, to);
        try {
            redis.opsForValue().set(key, result, ttl);
        } catch (RuntimeException e) {
            log.warn("Redis SET failed for {} ({}); continuing without caching", key, e.getMessage());
        }
    }

    private String key(Algorithm algorithm, String from, String to) {
        return String.format("route:%s:%s->%s",
                algorithm.apiName(),
                from.toLowerCase(Locale.ROOT),
                to.toLowerCase(Locale.ROOT));
    }
}
