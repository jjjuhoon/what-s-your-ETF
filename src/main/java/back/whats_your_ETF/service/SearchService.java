package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.StockRankResponse;
import back.whats_your_ETF.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 키워드로 주식 검색
    public List<StockRankResponse> searchStock(String userId, String stockName) {
        addSearch(userId, stockName);

        String pattern = "stock:*" + stockName + "*";
        Set<String> matchingKeys = redisTemplate.keys(pattern);

        if (matchingKeys == null || matchingKeys.isEmpty()) {
            throw new IllegalArgumentException("검색 결과가 없습니다: " + stockName);
        }

        List<StockRankResponse> results = new ArrayList<>();
        for (String key : matchingKeys) {
            Map<Object, Object> stockData = redisTemplate.opsForHash().entries(key);
            if (!stockData.isEmpty()) {
                results.add(new StockRankResponse(
                        (String) stockData.get("stockCode"),
                        (String) stockData.get("stockName"),
                        Long.valueOf((String) stockData.get("price")),
                        (String) stockData.get("priceChange")
                ));
            }
        }

        return results;
    }

    // 최근 검색어 추가
    public void addSearch(String userId, String stockName) {
        redisUtil.addRecentSearch(userId, stockName);
        redisUtil.setRecentSearchTTL(userId, 60 * 60 * 24 * 30); // 30일 TTL
    }

    // 최근 검색어 조회
    public List<String> getRecentSearches(String userId, int count) {
        return redisUtil.getRecentSearches(userId, count);
    }
}
