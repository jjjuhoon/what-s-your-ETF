package back.whats_your_ETF.service;

import back.whats_your_ETF.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RedisUtil redisUtil;

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
