package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.StockRankResponse;
import back.whats_your_ETF.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, String> redisTemplate;

    //최근 검색어 추가 및 Redis에서 stock 데이터 조회
    public StockRankResponse searchStock(String userId, String stockName) {

        addSearch(userId, stockName);

        String redisKey = "stock:" + stockName;
        Map<Object, Object> stockData = redisTemplate.opsForHash().entries(redisKey);

        if (stockData == null || stockData.isEmpty()) {
            throw new IllegalArgumentException("종목을 찾을 수 없습니다: " + stockName);
        }

        return new StockRankResponse(
                (String) stockData.get("stockCode"),
                (String) stockData.get("stockName"),
                Long.valueOf((String) stockData.get("price")),
                (String) stockData.get("priceChange")
        );
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
