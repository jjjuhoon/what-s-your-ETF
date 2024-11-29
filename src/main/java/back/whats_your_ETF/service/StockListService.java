package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.StockRankResponse;
import back.whats_your_ETF.dto.StockResponse;
import back.whats_your_ETF.entity.Ranking;
import back.whats_your_ETF.entity.Stock;
import back.whats_your_ETF.repository.EmitterRepository;
import back.whats_your_ETF.repository.RankingRepository;
import back.whats_your_ETF.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockListService {

    private final RankingRepository rankingRepository;
    private final EmitterRepository emitterRepository;
    private final StockRepository stockRepository;

    // SSE 연결 설정
    public SseEmitter subscribeToStockUpdates() {
        String id = "stock_updates_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(id, new SseEmitter(60L * 1000 * 60)); // 60분 연결 유지

        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        emitter.onTimeout(() -> emitterRepository.deleteById(id));

        return emitter;
    }

    // 5초마다 가격 변화를 감지하고 알림 전송
    @Scheduled(fixedRate = 5000)
    public void monitorPriceChanges() {
        List<Ranking> rankings = rankingRepository.findAll();
        for (Ranking ranking : rankings) {
            String currentPriceChangeRate = ranking.getPriceChangeRate();
            String cachedPriceChangeRate = emitterRepository.getCachedPriceChangeRate(ranking.getId());

            // 가격 변화 감지
            if (!currentPriceChangeRate.equals(cachedPriceChangeRate)) {
                StockRankResponse response = convertToStockResponse(ranking);
                sendPriceUpdate(response);

                emitterRepository.updateCachedPriceChangeRate(ranking.getId(), currentPriceChangeRate);
            }
        }
    }

    // 실시간 업데이트 전송
    private void sendPriceUpdate(StockRankResponse response) {
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById("stock_updates");
        sseEmitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event().id(key).name("price-update").data(response));
            } catch (IOException e) {
                emitterRepository.deleteById(key);
            }
        });
    }

    private StockRankResponse convertToStockResponse(Ranking ranking) {
        return new StockRankResponse(
                ranking.getStockCode(),
                ranking.getStockName(),
                ranking.getCurrentPrice() != null ? ranking.getCurrentPrice() : 0L,
                ranking.getPriceChangeRate()
        );
    }

    public List<StockRankResponse> getTop30ByFluctuation() {
        return rankingRepository.findTop30ByFluctuationRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockRankResponse> getTop30ByMarketCap() {
        return rankingRepository.findTop30ByMarketCapRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockRankResponse> getTop30ByProfit() {
        return rankingRepository.findTop30ByProfitAssetIndexRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockRankResponse> getTop30ByVolume() {
        return rankingRepository.findTop30ByVolumeRank()
                .stream()
                .limit(30)
                .map(this::convertToStockResponse)
                .collect(Collectors.toList());
    }
}
