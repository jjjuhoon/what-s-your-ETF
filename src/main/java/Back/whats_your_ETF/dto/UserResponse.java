package Back.whats_your_ETF.dto;

public record UserResponse(
        Long userId,
        String nickname,
        Long level,
        String image,
        Boolean member,
        Long asset,
        Long subscriberCount
) {}
