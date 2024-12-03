package back.whats_your_ETF.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class EtfRequest {
    @Getter
    @Setter

    public static class etfInvestList {
        List<etfInvest> etfList;
        String title;
        Long investmentAmount;
    }

    @Getter
    @Setter

    public static class etfInvest {
        String stockCode;
        String stockName;
        Long price;
        Double percentage;
    }
}
