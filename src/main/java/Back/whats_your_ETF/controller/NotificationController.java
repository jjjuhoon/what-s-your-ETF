package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.dto.PortfolioNotificationSingleRequest;
import Back.whats_your_ETF.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 연결
    @GetMapping(value = "/subscribe/{user_id}", produces = "text/event-stream")
    public SseEmitter subscribe(@PathVariable("user_id") Long userId) {
        return notificationService.subscribe(userId);
    }

    // Portfolio 알림 추가
    @PostMapping("/subscribe/portfolio")
    public ResponseEntity<Void> addPortfolioNotification(@RequestBody PortfolioNotificationSingleRequest request) {
        notificationService.addPortfolioNotification(request.userId(), request.portfolioId());
        return ResponseEntity.ok().build();
    }


}
