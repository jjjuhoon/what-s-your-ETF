package Back.whats_your_ETF.dto;

import lombok.*;

public record PortfolioResponse(
        Long portfolioId,
        String title,
        Long revenue,
        Long investAmount
) {}
