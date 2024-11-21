package back.whats_your_ETF.dto;

public record UserRankingResponse(
        Long userId,
        String nickname,
        Double revenuePercentage
) {}
