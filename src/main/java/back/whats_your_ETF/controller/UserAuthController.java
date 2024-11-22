package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.LoginResponse;
import back.whats_your_ETF.dto.LoginReqeust;
import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.SignUpResponse;
import back.whats_your_ETF.dto.TokenPair;
import back.whats_your_ETF.service.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    // 로그인 - AccessToken과 RefreshToken 반환
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginReqeust request) {
        TokenPair tokenPair = userAuthService.authenticate(request.userId(), request.password());
        return ResponseEntity.ok(new LoginResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(), request.userId()));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request) {
        userAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse("회원가입이 완료되었습니다.", request.nickname()));
    }

    // AccessToken 갱신
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody String refreshToken) {
        TokenPair newTokenPair = userAuthService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new LoginResponse(newTokenPair.getAccessToken(), newTokenPair.getRefreshToken(), null));
    }
}
