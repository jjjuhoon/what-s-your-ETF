package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.EtfRequest;
import back.whats_your_ETF.dto.PortfolioDetailsResponse;
import back.whats_your_ETF.dto.PortfolioListResponse;
import back.whats_your_ETF.dto.UserRankingResponse;
import back.whats_your_ETF.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    //2.1.1 포트폴리오 revenue 높은순으로 가져오기
    @GetMapping("/ranklist")
    public ResponseEntity<PortfolioListResponse> getPortfolioRank() {
        PortfolioListResponse response = etfService.getPortfolioRank();
        return ResponseEntity.ok(response);
    }

    //2.1.2 수익률 높은순으로 유저 랭킹
    @GetMapping("/user/ranking")
    public ResponseEntity<List<UserRankingResponse>> getUserRankings() {
        List<UserRankingResponse> rankings = etfService.getUserRanking();
        return ResponseEntity.ok(rankings);
    }



}
