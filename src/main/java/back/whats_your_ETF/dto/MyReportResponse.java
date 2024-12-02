package back.whats_your_ETF.dto;

import java.util.List;

public record MyReportResponse(
        String nickName,
        String portfolioTitle,
        Double portfolioRevenue,
        //각 종목을 List로
        List<StockPerformance> stockPerformances
) {
    // 종목 이름과 종목 수익률
    public record StockPerformance(String stockName, Double weightedYield) {}
}
