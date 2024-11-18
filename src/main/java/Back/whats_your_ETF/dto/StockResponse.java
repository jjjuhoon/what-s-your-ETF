package Back.whats_your_ETF.dto;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockResponse {

    private String stockCode;
    private String stockName;
    private Double percentage;
}