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

    //1.4.1 구독 목록 가져오기
    @GetMapping("/list/{user_id}")
    public ResponseEntity<List<SubscribeResponse>> getSubscriptions(@PathVariable("user_id") Long userId) {
        return subscribeService.getSubscriptionsByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
