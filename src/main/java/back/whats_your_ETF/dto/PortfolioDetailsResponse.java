package back.whats_your_ETF.dto;

import java.util.List;

public record PortfolioDetailsResponse(
        Long portfolioId,
        String title,
        Double revenue,
        Long investAmount,
        List<ETFStockResponse> etfStocks
) {}
