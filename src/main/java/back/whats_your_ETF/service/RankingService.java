package back.whats_your_ETF.service;

import back.whats_your_ETF.entity.Ranking;
import back.whats_your_ETF.repository.RankingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Service
@RequiredArgsConstructor
@Component
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RestTemplate restTemplate;
    private final AuthService authService;

    private static final String VOLUME_RANK_API_URL = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/volume-rank";
    private static final String FLUCTUATION_RANK_API_URL = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/fluctuation";
    private static final String PROFIT_ASSET_RANK_API_URL = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/profit-asset-index";
    private static final String MARKET_CAP_RANK_API_URL = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/market-cap";


    @Value("${APP_KEY}")
    private String appKey;

    @Value("${APP_SECRET}")
    private String appSecret;

    private HttpHeaders createHeaders(String trId) {
        String accessToken = authService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.set("custtype", "P");
        return headers;
    }

    private void saveOrUpdateRanking(Ranking ranking) {
        rankingRepository.findByStockName(ranking.getStockName().trim())
                .ifPresentOrElse(
                        existingRanking -> {
                            // 기존 값이 존재하면 업데이트
                            existingRanking.setVolumeRank(
                                    ranking.getVolumeRank() != null ? ranking.getVolumeRank() : existingRanking.getVolumeRank());
                            existingRanking.setFluctuationRank(
                                    ranking.getFluctuationRank() != null ? ranking.getFluctuationRank() : existingRanking.getFluctuationRank());
                            existingRanking.setProfitAssetIndexRank(
                                    ranking.getProfitAssetIndexRank() != null ? ranking.getProfitAssetIndexRank() : existingRanking.getProfitAssetIndexRank());
                            existingRanking.setMarketCapRank(
                                    ranking.getMarketCapRank() != null ? ranking.getMarketCapRank() : existingRanking.getMarketCapRank());
                            existingRanking.setCurrentPrice(ranking.getCurrentPrice());
                            rankingRepository.save(existingRanking);
                        },
                        () -> {
                            // 새로운 데이터는 새로 저장
                            rankingRepository.save(ranking);
                            System.out.println("Saved new ranking for stock: " + ranking.getStockName());
                        }
                );
    }


    public void updateVolumeRanking() {
        HttpHeaders headers = createHeaders("FHPST01710000");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = VOLUME_RANK_API_URL +
                "?FID_COND_MRKT_DIV_CODE=J" +
                "&FID_COND_SCR_DIV_CODE=20171" +
                "&FID_INPUT_ISCD=0000" +
                "&FID_DIV_CLS_CODE=0" +
                "&FID_BLNG_CLS_CODE=0" +
                "&FID_TRGT_CLS_CODE=111111111" +
                "&FID_TRGT_EXLS_CLS_CODE=0000000000" +
                "&FID_INPUT_PRICE_1=0" +
                "&FID_INPUT_PRICE_2=0" +
                "&FID_VOL_CNT=0" +
                "&FID_INPUT_DATE_1=0";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        processResponse(response, "volume");
    }

    public void updateFluctuationRanking() {
        HttpHeaders headers = createHeaders("FHPST01700000");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = FLUCTUATION_RANK_API_URL +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20170" +
                "&fid_input_iscd=0000" +
                "&fid_rank_sort_cls_code=0" +
                "&fid_input_cnt_1=0" +
                "&fid_prc_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_div_cls_code=0" +
                "&fid_rsfl_rate1=" +
                "&fid_rsfl_rate2=";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        processResponse(response, "fluctuation");
    }

    public void updateProfitAssetRanking() {
        HttpHeaders headers = createHeaders("FHPST01730000");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = PROFIT_ASSET_RANK_API_URL +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20173" +
                "&fid_input_iscd=0000" +
                "&fid_div_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_input_option_1=2023" +
                "&fid_input_option_2=0" +
                "&fid_rank_sort_cls_code=0" +
                "&fid_blng_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_trgt_cls_code=0";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        processResponse(response, "profit");
    }

    public void updateMarketCapRanking() {
        HttpHeaders headers = createHeaders("FHPST01740000");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = MARKET_CAP_RANK_API_URL +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20174" +
                "&fid_div_cls_code=0" +
                "&fid_input_iscd=0000" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=";

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        processResponse(response, "market_cap");
    }

    private void processResponse(ResponseEntity<Map> response, String rankType) {
        List<Map<String, Object>> output = (List<Map<String, Object>>) ((Map<String, Object>) response.getBody()).get("output");

        if (output == null || output.isEmpty()) {
            System.out.println("No data received for rank type: " + rankType);
            return;
        }

        // 해당 rankType에 맞는 컬럼 초기화
        resetRankingColumn(rankType);

        for (Map<String, Object> rankData : output) {
            try {
                String stockName = (String) rankData.get("hts_kor_isnm");
                String stockCode = (String) rankData.get("mksc_shrn_iscd");
                if (stockCode == null) {
                    stockCode = (String) rankData.get("stck_shrn_iscd"); // 대체 필드 사용
                }
                String currentPriceStr = (String) rankData.get("stck_prpr");
                String rankStr = (String) rankData.get("data_rank");

                if (stockCode == null) {
                    System.out.println("Missing stockCode for stock: " + stockName);
                    continue; // Skip if stockCode is missing
                }

                if (stockName == null || currentPriceStr == null || rankStr == null) {
                    System.out.println("Missing critical data for stock: " + stockName);
                    continue; // Skip incomplete data
                }

                Ranking ranking = rankingRepository.findByStockCode(stockCode)
                        .orElse(new Ranking());

                ranking.setStockName(stockName);
                ranking.setStockCode(stockCode);
                ranking.setCurrentPrice(Long.parseLong(currentPriceStr));

                switch (rankType) {
                    case "volume":
                        ranking.setVolumeRank(parseRank(rankStr));
                        break;
                    case "fluctuation":
                        ranking.setFluctuationRank(parseRank(rankStr));
                        break;
                    case "profit":
                        ranking.setProfitAssetIndexRank(parseRank(rankStr));
                        break;
                    case "market_cap":
                        ranking.setMarketCapRank(parseRank(rankStr));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown rank type: " + rankType);
                }

                rankingRepository.save(ranking);
            } catch (Exception e) {
                System.err.println("Error processing data: " + rankData + ", Error: " + e.getMessage());
            }
        }
    }
    private void resetRankingColumn(String rankType) {
        List<Ranking> rankings = rankingRepository.findAll();
        for (Ranking ranking : rankings) {
            switch (rankType) {
                case "volume":
                    ranking.setVolumeRank(null);
                    break;
                case "fluctuation":
                    ranking.setFluctuationRank(null);
                    break;
                case "profit":
                    ranking.setProfitAssetIndexRank(null);
                    break;
                case "market_cap":
                    ranking.setMarketCapRank(null);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown rank type: " + rankType);
            }
        }
        rankingRepository.saveAll(rankings);
    }


    private Integer parseRank(Object rank) {
        return rank != null ? Integer.parseInt(rank.toString()) : null;
    }
}
