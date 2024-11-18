package Back.whats_your_ETF.controller;

import Back.whats_your_ETF.entity.User;
import Back.whats_your_ETF.dto.UserResponse;
import Back.whats_your_ETF.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userinfo")
public class UserController {

    private final UserService userService;

    // 사용자 정보 불러오기
    @GetMapping("/{user_id}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable("user_id") Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(new UserResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getLevel(),
                        user.getImage(),
                        user.getMember(),
                        user.getAsset(),
                        user.getSubscriberCount()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    //멤버십 가입하기
    @PostMapping("/membership/{user_id}")
    public ResponseEntity<String> updateMembership(@PathVariable("user_id") Long userId) {
        boolean updated = userService.updateMembership(userId);

        if (updated) {
            return ResponseEntity.ok("회원 상태로 업데이트되었습니다.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
