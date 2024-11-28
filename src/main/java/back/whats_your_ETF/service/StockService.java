package back.whats_your_ETF.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void updateStockData() {
        String[] markets = {"KOSPI", "KOSDAQ"}; // 시장 배열

        for (String market : markets) {
            String apiUrl = "https://finance.daum.net/api/quotes/sectors?fieldName=&order=&perPage=&market="
                    + market
                    + "&page=&changes=UPPER_LIMIT,RISE,EVEN,FALL,LOWER_LIMIT";

            try {
                // 요청 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
                headers.set("Referer", "https://finance.daum.net/domestic/all_stocks");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                // RestTemplate 요청
                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    List<Map<String, Object>> sectors = (List<Map<String, Object>>) body.get("data");

                    Set<String> processedStockNames = new HashSet<>(); // 이미 처리된 stockName 저장

                    for (Map<String, Object> sector : sectors) {
                        List<Map<String, Object>> includedStocks = (List<Map<String, Object>>) sector.get("includedStocks");

                        for (Map<String, Object> stockData : includedStocks) {
                            String stockCode = (String) stockData.get("symbolCode");
                            if (stockCode != null && stockCode.startsWith("A")) {
                                stockCode = stockCode.substring(1); // 'A' 제거
                            }

                            String stockName = (String) stockData.get("name");
                            Long price = stockData.get("tradePrice") != null
                                    ? Math.round(Double.valueOf(stockData.get("tradePrice").toString()))
                                    : 0L;

                            // changeRate 추출 및 변환
                            String priceChange = null;
                            Object changeRateObj = stockData.get("changeRate");
                            if (changeRateObj instanceof Double) {
                                priceChange = String.format("%.2f", (Double) changeRateObj * 100); // 소수점 둘째 자리까지 변환
                            } else if (changeRateObj instanceof String) {
                                priceChange = String.format("%.2f", Double.valueOf((String) changeRateObj) * 100);
                            }

                            if (stockName != null && stockCode != null) {
                                // 중복된 stockName 무시
                                if (processedStockNames.contains(stockName)) {
                                    continue;
                                }
                                processedStockNames.add(stockName);

                                // Redis에 저장 (stockName을 키로 사용)
                                String redisKey = "stock:" + stockName;
                                redisTemplate.opsForHash().put(redisKey, "stockName", stockName);
                                redisTemplate.opsForHash().put(redisKey, "stockCode", stockCode);
                                redisTemplate.opsForHash().put(redisKey, "price", String.valueOf(price));
                                redisTemplate.opsForHash().put(redisKey, "priceChange", priceChange);

                                // Redis 데이터에 TTL(Time-To-Live) 설정 (예: 1일)
                                redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
                            }
                        }
                    }
                }
            } catch (HttpServerErrorException e) {
                if (e.getMessage().contains("초당 거래건수를 초과")) {
                    System.err.println("API 요청 제한 초과: " + e.getMessage());
                    try {
                        Thread.sleep(1000 * 60); // 1분 대기
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("API 호출 중 예외 발생: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("주식 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            }
        }
    }
}

