package Back.whats_your_ETF.dto;

public record StockResponse(
        String stockCode,
        String stockName,
        Double percentage
) {}
