package back.whats_your_ETF.controller;

import back.whats_your_ETF.entity.Ranking;
import back.whats_your_ETF.repository.RankingRepository;
import back.whats_your_ETF.service.SearchService;
import lombok.RequiredArgsConstructor;
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
    private final RankingRepository rankingRepository; // DB에서 종목 정보 조회

    // 검색
    @GetMapping
    public ResponseEntity<Ranking> searchStock(@RequestParam String userId, @RequestParam String stockName) {

        searchService.addSearch(userId, stockName);

        Ranking stockInfo = rankingRepository.findByStockName(stockName)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다."));

        return ResponseEntity.ok(stockInfo);
    }

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecentSearches(@RequestParam String userId, @RequestParam(defaultValue = "10") int count) {
        List<String> recentSearches = searchService.getRecentSearches(userId, count);
        return ResponseEntity.ok(recentSearches);
    }
}
