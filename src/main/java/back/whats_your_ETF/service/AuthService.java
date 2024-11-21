package back.whats_your_ETF.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private String cachedAccessToken;
    private LocalDateTime tokenExpiryTime;

    private static final String TOKEN_URL = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";
    private static final String APP_KEY = "PSmqu4Sv0FyEaup0qheHCN8ypL0y7L7jMx2R";
    private static final String APP_SECRET = "VeF7GB6itEg6Oax5N9TrSg31PF6+9lAsFyRiH3uDCNQE89fpTRjxyp1Q8DcAcef0gZNVDI/AwiaOUHDC0yqIZVnbKhHhuU84gRkz16p3XrAXnDLHLU+XlEjvSeJZh+/8kxE0tfLkKTz6oNCXT5H5qzFvThgdvuQkCMt15Ifja7/ksD6AtG8=";

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Access Token을 반환하며, 만료된 경우 새로 갱신.
     */
    public String getAccessToken() {
        if (isTokenValid()) {
            return cachedAccessToken;
        }
        return requestNewAccessToken();
    }

    /**
     * 토큰 유효성 검사
     */
    private boolean isTokenValid() {
        return cachedAccessToken != null && tokenExpiryTime != null && LocalDateTime.now().isBefore(tokenExpiryTime);
    }

    /**
     * 새로운 Access Token 요청
     */
    private String requestNewAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "appkey", APP_KEY,
                "appsecret", APP_SECRET
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            cachedAccessToken = (String) responseBody.get("access_token");
            int expiresInSeconds = (int) responseBody.get("expires_in");

            // 만료 시간 설정
            tokenExpiryTime = LocalDateTime.now().plus(expiresInSeconds, ChronoUnit.SECONDS);
        }

        return cachedAccessToken;
    }
}
