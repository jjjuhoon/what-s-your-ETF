package Back.whats_your_ETF;

import Back.whats_your_ETF.entity.Ranking;
import Back.whats_your_ETF.repository.RankingRepository;
import Back.whats_your_ETF.service.AuthService;
import Back.whats_your_ETF.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TestHelper {

    private final RestTemplate restTemplate;
    private final AuthService authService;
    private final RankingRepository rankingRepository;
    private final RankingService rankingService;

    private static final String PROFIT_ASSET_RANK_API_URL = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/profit-asset-index";

    private HttpHeaders createHeaders(String trId) {
        String accessToken = authService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appkey", "PSmqu4Sv0FyEaup0qheHCN8ypL0y7L7jMx2R"); // 샘플 키
        headers.set("appsecret", "VeF7..."); // 샘플 시크릿
        headers.set("tr_id", trId);
        return headers;
    }

    // 1. API 호출 테스트
    public void testApiCall() {
        HttpHeaders headers = createHeaders("FHPST01630000");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = PROFIT_ASSET_RANK_API_URL + "?FID_COND_MRKT_DIV_CODE=J&custtype=P";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            System.out.println("API Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("API 호출 실패: " + e.getMessage());
        }
    }

    // 2. 인증 테스트
    public void testAuthService() {
        try {
            String token = authService.getAccessToken();
            System.out.println("Access Token: " + token);
        } catch (Exception e) {
            System.err.println("토큰 발급 실패: " + e.getMessage());
        }
    }

    // 3. DB 저장 테스트
    public void testDatabaseInsert() {
        Ranking ranking = new Ranking();
        ranking.setStockCode("TEST_CODE");
        ranking.setStockName("Test Stock");
        ranking.setCurrentPrice(10000L);
        ranking.setCollectedAt(LocalDateTime.now());
        ranking.setVolumeRank(1);
        ranking.setVolumeInfo("1000");

        try {
            rankingRepository.save(ranking);
            System.out.println("데이터 저장 성공!");
        } catch (Exception e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
        }
    }

    // 4. Volume Ranking 업데이트 로직 검증용
    @Transactional
    public void testUpdateVolumeRanking() {
        try {
            rankingService.updateVolumeRanking();
            System.out.println("Volume Ranking 업데이트 성공!");
        } catch (Exception e) {
            System.err.println("Volume Ranking 업데이트 실패: " + e.getMessage());
        }
    }
}
