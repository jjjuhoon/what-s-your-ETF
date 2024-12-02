package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.PortfolioNotificationDeleteRequest;
import back.whats_your_ETF.dto.PortfolioNotificationSingleRequest;
import back.whats_your_ETF.service.NotificationService;
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
        notificationService.addPortfolioNotification(request);
        return ResponseEntity.ok().build();
    }

    // Portfolio 알림 삭제
    @DeleteMapping("/subscribe/portfolio")
    public ResponseEntity<Void> deletePortfolioNotification(@RequestBody PortfolioNotificationDeleteRequest request) {
        notificationService.deletePortfolioNotification(request);
        return ResponseEntity.ok().build();
    }

}
