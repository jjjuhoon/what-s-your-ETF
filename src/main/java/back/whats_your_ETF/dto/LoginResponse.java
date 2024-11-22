package back.whats_your_ETF.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String userId
){}
