package back.whats_your_ETF.dto;

public record SubscribeResponse(
    Long publisher_id,
    String nickname,
    Long revenue
){}
