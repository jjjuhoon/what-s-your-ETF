package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.LoginResponse;
import back.whats_your_ETF.dto.LoginReqeust;
import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.SignUpResponse;
import back.whats_your_ETF.dto.TokenPair;
import back.whats_your_ETF.service.UserAuthService;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

        // AccessToken 쿠키 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokenPair.getAccessToken())
                .httpOnly(true)
                // .secure(true) // HTTPS 환경에서 활성화
                .path("/")
                .maxAge(60 * 60) // 1시간
                // .sameSite("Strict") // HTTPS 환경에서 활성화
                .build();

        // RefreshToken 쿠키 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenPair.getRefreshToken())
                .httpOnly(true)
                // .secure(true) // HTTPS 환경에서 활성화
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                // .sameSite("Strict") // HTTPS 환경에서 활성화
                .build();

        // LoginResponse 생성
        LoginResponse loginResponse = new LoginResponse(
                request.userId() // userId 반환
        );

        // 응답에 쿠키와 userId 포함
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                .body(loginResponse);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken) {
        try {
            // DB에서 Refresh Token 제거
            userAuthService.logoutByRefreshToken(refreshToken);

            // 클라이언트 쿠키 무효화를 위해 빈 쿠키 반환
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }




    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request) {
        userAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse("회원가입이 완료되었습니다.", null)); // userId 제거
    }


    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Refresh Token 없으면 400
        }

        try {
            TokenPair newTokenPair = userAuthService.refreshAccessToken(refreshToken);

            // 새 Access Token 쿠키 설정
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", newTokenPair.getAccessToken())
                    .httpOnly(true)
                    .path("/")
                    .maxAge(60 * 60) // 1시간
                    .build();

            ResponseCookie refreshCookie = null;
            if (userAuthService.shouldRefreshRefreshToken(refreshToken)) {
                refreshCookie = ResponseCookie.from("refreshToken", newTokenPair.getRefreshToken())
                        .httpOnly(true)
                        .path("/")
                        .maxAge(60 * 60 * 24 * 7) // 7일
                        .build();
            }

            if (refreshCookie != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                        .build();
            } else {
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Refresh Token 유효하지 않을 때
        }
    }


    // 중복 체크 (userId와 nickname)
    @GetMapping("/check-duplicate")
    public ResponseEntity<String> checkDuplicate(@RequestParam String field, @RequestParam String value) {
        boolean isDuplicate = userAuthService.checkDuplicate(field, value);
        if (isDuplicate) {
            String fieldName = "userId".equals(field) ? "아이디" : "닉네임";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(fieldName + "가 이미 사용 중입니다.");
        }
        String fieldName = "userId".equals(field) ? "아이디" : "닉네임";
        return ResponseEntity.ok(fieldName + "는 사용 가능합니다.");
    }

}
