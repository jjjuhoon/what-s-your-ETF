package back.whats_your_ETF.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    // 기존 메서드
    public void save(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration));
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // ----------------------
    // 최근 검색어 관련 메서드 추가
    // ----------------------

    // 검색어 추가 (Sorted Set 사용)
    public void addRecentSearch(String userId, String stockName) {
        String key = "recent_search:" + userId;
        Long score = System.currentTimeMillis(); // 타임스탬프를 점수로 사용
        redisTemplate.opsForZSet().add(key, stockName, score);
    }

    // 최근 검색어 조회
    public List<String> getRecentSearches(String userId, int count) {
        String key = "recent_search:" + userId;
        Set<String> results = redisTemplate.opsForZSet().reverseRange(key, 0, count - 1); // 최신순 조회
        return results == null ? new ArrayList<>() : new ArrayList<>(results);
    }

    // 최근 검색어 TTL 설정
    public void setRecentSearchTTL(String userId, long durationInSeconds) {
        String key = "recent_search:" + userId;
        redisTemplate.expire(key, Duration.ofSeconds(durationInSeconds));
    }
}
