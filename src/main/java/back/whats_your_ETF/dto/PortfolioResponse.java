package back.whats_your_ETF.dto;

public record PortfolioResponse(
        Long portfolioId,
        String title,
        Long revenue,
        Long investAmount
) {}
