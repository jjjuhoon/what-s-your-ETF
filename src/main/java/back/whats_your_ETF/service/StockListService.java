package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.StockResponse;
import back.whats_your_ETF.entity.Ranking;
import back.whats_your_ETF.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockListService {

    private final RankingRepository rankingRepository;

    public List<StockResponse> getTop30ByFluctuation() {
        return rankingRepository.findTop30ByFluctuationRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockResponse> getTop30ByMarketCap() {
        return rankingRepository.findTop30ByMarketCapRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockResponse> getTop30ByProfit() {
        return rankingRepository.findTop30ByProfitAssetIndexRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockResponse> getTop30ByVolume() {
        return rankingRepository.findTop30ByVolumeRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    private StockResponse convertToStockResponse(Ranking ranking) {
        return new StockResponse(
                ranking.getStockCode(),
                ranking.getStockName(),
                ranking.getCurrentPrice() != null ? ranking.getCurrentPrice().doubleValue() : 0.0
        );
    }
}
