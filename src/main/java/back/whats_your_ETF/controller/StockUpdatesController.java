package back.whats_your_ETF.controller;

import back.whats_your_ETF.service.StockListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-updates")
public class StockUpdatesController {

    private final StockListService stockListService;

    @GetMapping("/subscribe")
    public SseEmitter subscribeToStockUpdates() {
        return stockListService.subscribeToStockUpdates();
    }
}
