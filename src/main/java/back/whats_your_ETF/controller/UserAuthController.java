package back.whats_your_ETF.controller;

import back.whats_your_ETF.dto.LoginResponse;
import back.whats_your_ETF.dto.LoginReqeust;
import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.SignUpResponse;
import back.whats_your_ETF.dto.TokenPair;

import back.whats_your_ETF.service.UserAuthService;
import back.whats_your_ETF.util.JwtUtil;
import back.whats_your_ETF.util.RedisUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final JwtUtil jwtUtil;

    public UserAuthController(UserAuthService userAuthService, JwtUtil jwtUtil, final RedisUtil redisUtil) {
        this.userAuthService = userAuthService;
        this.jwtUtil = jwtUtil;
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
                // .sameSite("Strict")
                .build();

        // RefreshToken 쿠키 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenPair.getRefreshToken())
                .httpOnly(true)
                // .secure(true) // HTTPS 환경에서 활성화
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                // .sameSite("Strict")
                .build();

        Long id = userAuthService.getId(request.userId());


        // LoginResponse 생성
        LoginResponse loginResponse = new LoginResponse(
                id // id 반환
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
        Long id = userAuthService.getId(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse("회원가입이 완료되었습니다.", id));
    }


    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestParam String userId, @CookieValue(value = "accessToken", required = false) String accessToken, @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Refresh Token 없으면 400
        }

        try {
            // refresh 토큰이 존재하고, 만료되지 않았을 때
            if (userAuthService.validateRefreshToken(userId, refreshToken) && !jwtUtil.isTokenExpired(refreshToken)) {
                // accessToken이 만료되었다면 accessToken을 갱신
                if (jwtUtil.isTokenExpired(accessToken)) {
                    String newAccessToken = jwtUtil.generateAccessToken(userId);

                    ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                            .httpOnly(true)
                            .path("/")
                            .maxAge(60 * 60) // 1시간
                            .build();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshToken)
                            .build();

                }

                // 만료 되지 않았다면 accessToken을 갱신하지 않고 아무런 행동도 취하지 않음.

            }

            // refresh 토큰이 만료되었을 때는 그냥 로그인을 통해서 갱신해야함.

            return ResponseEntity.ok().build();
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 " + fieldName + " 입니다.");
        }
        String fieldName = "userId".equals(field) ? "아이디" : "닉네임";
        return ResponseEntity.ok("사용 가능한 " + fieldName + " 입니다.");
    }

}
