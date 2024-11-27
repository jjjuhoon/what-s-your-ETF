package back.whats_your_ETF.dto;

public record StockRankResponse (
        String stockCode,
        String stockName,
        Double price,
        Double priceChangeRate
) {}
