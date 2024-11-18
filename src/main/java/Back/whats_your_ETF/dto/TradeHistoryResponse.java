package Back.whats_your_ETF.dto;

import java.util.List;

public record TradeHistoryResponse(
        Long portfolioId,
        String title,
        Long revenue,
        Long investAmount,
        List<StockResponse> stocks
) {}
