package back.whats_your_ETF.dto;

public record ETFStockResponse(
        String stockCode,
        String stockName,
        Double percentage,
        Long purchasePrice
) {}
