package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.PortfolioNotificationDeleteRequest;
import back.whats_your_ETF.dto.PortfolioNotificationSingleRequest;
import back.whats_your_ETF.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> addPortfolioNotification(@RequestBody PortfolioNotificationSingleRequest request) {
        try {
            notificationService.addPortfolioNotification(request);
            return ResponseEntity.ok("알림 설정이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


    // Portfolio 알림 삭제
    @DeleteMapping("/subscribe/portfolio")
    public ResponseEntity<Void> deletePortfolioNotification(@RequestBody PortfolioNotificationDeleteRequest request) {
        notificationService.deletePortfolioNotification(request);
        return ResponseEntity.ok().build();
    }

}
