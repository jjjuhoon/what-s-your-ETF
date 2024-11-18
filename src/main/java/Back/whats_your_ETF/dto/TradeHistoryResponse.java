package Back.whats_your_ETF.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeHistoryResponse {
    private Long portfolioId;
    private String title;
    private Long revenue;
    private Long investAmount;
    private List<StockResponse> stocks;
}