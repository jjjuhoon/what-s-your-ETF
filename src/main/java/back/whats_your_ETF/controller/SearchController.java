package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.StockRankResponse;
import back.whats_your_ETF.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final StringRedisTemplate redisTemplate;

    //검색
    @GetMapping
    public ResponseEntity<StockRankResponse> searchStock(
            @RequestParam String userId,
            @RequestParam String stockName
    ) {
        // 최근 검색어 저장 및 Redis 데이터 조회
        StockRankResponse stockRankResponse = searchService.searchStock(userId, stockName);
        return ResponseEntity.ok(stockRankResponse);
    }

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecentSearches(@RequestParam String userId, @RequestParam(defaultValue = "10") int count) {
        List<String> recentSearches = searchService.getRecentSearches(userId, count);
        return ResponseEntity.ok(recentSearches);
    }
}
