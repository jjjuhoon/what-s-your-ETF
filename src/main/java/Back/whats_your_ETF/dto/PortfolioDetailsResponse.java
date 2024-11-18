package Back.whats_your_ETF.dto;

import java.util.List;

public record PortfolioDetailsResponse(
        Long portfolioId,
        String title,
        Long revenue,
        Long investAmount,
        List<ETFStockResponse> etfStocks
) {}
