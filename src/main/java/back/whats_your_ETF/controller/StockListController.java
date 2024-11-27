package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.StockRankResponse;
import back.whats_your_ETF.service.StockListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stockList")
@RequiredArgsConstructor
public class StockListController {

    private final StockListService stockListService;

    @GetMapping("/top30/volume")
    public ResponseEntity<List<StockRankResponse>> getTop30ByVolume() {
        return ResponseEntity.ok(stockListService.getTop30ByVolume());
    }

    @GetMapping("/top30/fluctuation")
    public ResponseEntity<List<StockRankResponse>> getTop30ByFluctuation() {
        return ResponseEntity.ok(stockListService.getTop30ByFluctuation());
    }

    @GetMapping("/top30/profit")
    public ResponseEntity<List<StockRankResponse>> getTop30ByProfit() {
        return ResponseEntity.ok(stockListService.getTop30ByProfit());
    }

    @GetMapping("/top30/market_cap")
    public ResponseEntity<List<StockRankResponse>> getTop30ByMarketCap() {
        return ResponseEntity.ok(stockListService.getTop30ByMarketCap());
    }
}
