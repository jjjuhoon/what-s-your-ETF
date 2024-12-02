package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.MyReportResponse;
import back.whats_your_ETF.dto.PortfolioListResponse;
import back.whats_your_ETF.dto.TradeHistoryResponse;
import back.whats_your_ETF.dto.UserResponse;
import back.whats_your_ETF.service.EtfService;
import back.whats_your_ETF.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userinfo")
public class UserController {

    private final UserService userService;
    private final EtfService etfService;

    // 사용자 정보 불러오기
    @GetMapping("/{user_id}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable("user_id") Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //멤버십 가입하기
    @PostMapping("/membership/{user_id}")
    public ResponseEntity<String> updateMembership(@PathVariable("user_id") Long userId) {
        return userService.updateMembership(userId);
    }

    //멤버십 해지하기
    @DeleteMapping("/membership/{user_id}")
    public ResponseEntity<String> deleteMembership(@PathVariable("user_id") Long userId) {
        return userService.deleteMembership(userId);
    }

    //1.2.1 : 나의 거래내역 가져오기
    @GetMapping("/tradehistory/{user_id}")
    public ResponseEntity<List<TradeHistoryResponse>> getTradeHistory(@PathVariable("user_id") Long userId) {
        return userService.getTradeHistoryById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //1.2.2 : 나의 분석 리포트 가져오기
    @GetMapping("/myreport/{user_id}")
    public ResponseEntity<MyReportResponse> getMyReport(@PathVariable("user_id") Long userId) {
        return userService.getMyReport(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //1.3.1 : 나의 ETF목록 가져오기
    @GetMapping("/etf/list/{user_id}")
    public ResponseEntity<PortfolioListResponse> getUserETFlist(@PathVariable("user_id") Long userId) {
        return userService.getUserETFlistById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 사용자 수익률 조회
    @GetMapping("/{user_id}/revenue-percentage")
    public ResponseEntity<Double> getUserRevenuePercentage(@PathVariable("user_id") Long userId) {
        return etfService.getUserRevenuePercentage(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
