package back.whats_your_ETF.dto;


public record PortfolioNotificationSingleRequest(
        Long userId,
        Long portfolioId,
        Long profitSpot,
        Long lossSpot
) {
}
