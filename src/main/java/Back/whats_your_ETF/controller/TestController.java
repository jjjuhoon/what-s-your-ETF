package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.TestHelper;
import Back.whats_your_ETF.entity.Ranking;
import Back.whats_your_ETF.repository.RankingRepository;
import Back.whats_your_ETF.service.AuthService;
import Back.whats_your_ETF.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestHelper testHelper;

    @GetMapping("/test/api")
    public void testApiCall() {
        testHelper.testApiCall();
    }

    @GetMapping("/test/auth")
    public void testAuthService() {
        testHelper.testAuthService();
    }

    @GetMapping("/test/db")
    public void testDatabaseInsert() {
        testHelper.testDatabaseInsert();
    }

    @GetMapping("/test/update-volume")
    public void testUpdateVolumeRanking() {
        testHelper.testUpdateVolumeRanking();
    }
}

