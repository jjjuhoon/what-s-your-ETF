package back.whats_your_ETF.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final StringRedisTemplate redisTemplate;

    /**
     * 특정 key의 값을 조회
     * @param key Redis key
     * @return key에 해당하는 값
     */
    @GetMapping("/redis/get")
    public ResponseEntity<Object> getKey(@RequestParam String key) {
        try {
            System.out.println("Fetching key: " + key); // 로그 추가
            Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
            System.out.println("Result: " + result); // 결과 로그
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("isSuccess", false, "code", "REDIS404", "message", "Key not found", "result", null));
            }
            return ResponseEntity.ok(Map.of("isSuccess", true, "code", "REDIS200", "message", "Success", "result", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("isSuccess", false, "code", "COMMON5000", "message", "서버 에러, 관리자에게 문의 바랍니다.", "result", e.getMessage()));
        }
    }


    /**
     * Redis에 저장된 모든 키 목록 조회
     * @return 모든 키 목록
     */
    @GetMapping("/redis/keys")
    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }
}
