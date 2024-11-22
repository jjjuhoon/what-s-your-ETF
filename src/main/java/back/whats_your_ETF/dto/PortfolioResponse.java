package back.whats_your_ETF.dto;

public record PortfolioResponse(
        Long portfolioId,
        String title,
        Double revenue,
        Long investAmount
) {}
