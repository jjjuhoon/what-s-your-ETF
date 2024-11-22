package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.SubscribeResponse;
import back.whats_your_ETF.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 1.4.5구독하기
    @PostMapping("/{subscriberId}/{publisherId}")
    public ResponseEntity<Void> subscribe(@PathVariable Long subscriberId, @PathVariable Long publisherId) {
        subscribeService.subscribe(subscriberId, publisherId);
        return ResponseEntity.ok().build();
    }

    // 1.4.3 구독 취소하기
    @DeleteMapping("/{subscriberId}/{publisherId}")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long subscriberId, @PathVariable Long publisherId) {
        subscribeService.unsubscribe(subscriberId, publisherId);
        return ResponseEntity.ok().build();
    }
}
