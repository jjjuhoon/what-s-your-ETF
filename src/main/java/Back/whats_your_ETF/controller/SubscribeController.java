package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.dto.SubscribeResponse;
import Back.whats_your_ETF.service.SubscribeService;
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

    //1.4.3 구독 취소하기
    @DeleteMapping("/{subscriber_id}/{publisher_id}")
    public ResponseEntity<String> unsubscribe(@PathVariable("subscriber_id") Long subscriberId, @PathVariable("publisher_id") Long publisherId) {
        boolean success = subscribeService.unsubscribe(subscriberId, publisherId);

        if (success) {
            return ResponseEntity.ok("구독 취소 성공");
        } else {
            return ResponseEntity.badRequest().body("구독 관계가 존재하지 않습니다");
        }
    }
}
