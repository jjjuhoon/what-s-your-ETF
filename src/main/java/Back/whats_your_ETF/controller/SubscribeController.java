package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.dto.SubscribeResponse;
import Back.whats_your_ETF.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscribe")
public class SubscribeController {

    private final SubscribeService subscribeService;

    @GetMapping("/list/{user_id}")
    public ResponseEntity<List<SubscribeResponse>> getSubscriptions(@PathVariable("user_id") Long userId){
        List<SubscribeResponse> subscriptions = subscribeService.getSubscriptionsByUserId(userId);

        if (subscriptions.isEmpty()) {
            return ResponseEntity.noContent().build(); // 구독 목록이 없을 경우 204 응답
        }

        return ResponseEntity.ok(subscriptions); // 성공적으로 구독 목록 반환
    }
}
