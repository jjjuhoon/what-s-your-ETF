package back.whats_your_ETF.controller;

import back.whats_your_ETF.TestHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

