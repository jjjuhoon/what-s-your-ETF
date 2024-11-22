package back.whats_your_ETF.dto;

public record UserRankingResponse(
        Long userId,
        String nickname,
        String image,
        Double totalRevenue,
        Double revenuePercentage
) {}
