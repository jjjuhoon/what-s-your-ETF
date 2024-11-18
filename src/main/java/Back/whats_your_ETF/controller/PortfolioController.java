package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.dto.PortfolioDetailsResponse;
import Back.whats_your_ETF.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 1.3.2 ETF 상세보기
    @GetMapping("/details/{portfolio_id}")
    public ResponseEntity<PortfolioDetailsResponse> getETFDetails(@PathVariable("portfolio_id") Long portfolioId) {
        return portfolioService.getPortfolioDetailsByPortfolioId(portfolioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
