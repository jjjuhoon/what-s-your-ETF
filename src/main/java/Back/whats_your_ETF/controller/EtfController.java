package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.dto.EtfRequest;
import Back.whats_your_ETF.dto.PortfolioDetailsResponse;
import Back.whats_your_ETF.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/etf")
public class EtfController {

    private final EtfService etfService;

    // 1.3.2 ETF 상세보기
    @GetMapping("/details/{portfolio_id}")
    public ResponseEntity<PortfolioDetailsResponse> getETFDetails(@PathVariable("portfolio_id") Long portfolioId) {
        return etfService.getPortfolioDetailsByPortfolioId(portfolioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 1.3.4 : ETF 투자하기
    @PostMapping("/buy/{user_id}")
    public ResponseEntity<Void> buyETF(@PathVariable("user_id") Long userId,
                                       @RequestBody EtfRequest.etfInvestList etfInvestList) {
        etfService.buyETF(userId, etfInvestList);
        return ResponseEntity.ok().build();
    }

    // 1.3.5 : ETF 매도하기
    @DeleteMapping("/sell/{portfolio_id}")
    public ResponseEntity<Void> sellETF(@PathVariable("portfolio_id") Long portfolioId) {
        etfService.sellETF(portfolioId);
        return ResponseEntity.ok().build();
    }


}
