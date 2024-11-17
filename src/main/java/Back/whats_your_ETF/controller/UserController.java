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
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // 사용자 정보 불러오기
    @GetMapping("/userinfo/{user_id}")
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
}
