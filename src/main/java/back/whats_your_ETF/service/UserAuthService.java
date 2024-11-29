package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.TokenPair;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.UserRepository;
import back.whats_your_ETF.util.JwtUtil;
import back.whats_your_ETF.util.RedisUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserAuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    private static final long REFRESH_TOKEN_TTL = 60 * 60 * 24 * 7; // 7일

    public UserAuthService(JwtUtil jwtUtil, UserRepository userRepository,
                           RedisUtil redisUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // 사용자 인증 및 토큰 발급
    public TokenPair authenticate(String userId, String password) {
        // 사용자 확인
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        // 기존 Refresh Token 체크
        String storedToken = redisUtil.get(userId);
        if (storedToken != null && !jwtUtil.isTokenExpired(storedToken)) {
            String newAccessToken = jwtUtil.generateAccessToken(userId);

            // 기존 Refresh Token과 새 Access Token 반환
            return new TokenPair(newAccessToken, storedToken);
        }

        // AccessToken과 RefreshToken 생성
        TokenPair tokenPair = jwtUtil.generateToken(userId);

        // RefreshToken 저장 (Redis에 저장하거나 갱신)
        saveOrUpdateRefreshToken(userId, tokenPair.getRefreshToken());

        return tokenPair;
    }

    public void logoutByRefreshToken(String refreshToken) {
        // Refresh Token 유효성 검사
        String userId = jwtUtil.extractUserId(refreshToken);
        if (!validateRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token");
        }

        // Refresh Token 삭제 (Redis에서 삭제)
        redisUtil.delete(userId);
    }

    // 회원가입
    public void signup(SignUpRequest signUpRequest) {
        if (userRepository.findByUserId(signUpRequest.userId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 ID입니다.");
        }

        long min = 1_000_000_000L;
        long max = 2_000_000_000L;

        User user = User.builder()
                .userId(signUpRequest.userId())
                .password(passwordEncoder.encode(signUpRequest.password()))
                .nickname(signUpRequest.nickname())
                .isInTop10(false)
                .level(1L)
                .member(false)
                .asset((ThreadLocalRandom.current().nextLong(min, max)))
                .subscriberCount(0L)
                .build();

        userRepository.save(user);
    }

    // RefreshToken 저장 또는 갱신
    private void saveOrUpdateRefreshToken(String userId, String refreshToken) {
        redisUtil.save(userId, refreshToken, REFRESH_TOKEN_TTL);
    }

    // RefreshToken 검증
    public boolean validateRefreshToken(String userId, String refreshToken) {
        String storedToken = redisUtil.get(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    // 중복 체크
    public boolean checkDuplicate(String field, String value) {
        if ("userId".equalsIgnoreCase(field)) {
            return userRepository.findByUserId(value).isPresent();
        } else if ("nickname".equalsIgnoreCase(field)) {
            return userRepository.findByNickname(value).isPresent();
        }
        throw new IllegalArgumentException("지원하지 않는 필드입니다: " + field);
    }

    // id값 반환
    public Long getId(String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user == null) {
            throw new IllegalArgumentException("존재하지 않는 userId입니다");

        }
        return user.getId();
    }

    // nickname 값 반환
    public String getNickname(String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user == null) {
            throw new IllegalArgumentException("존재하지 않는 userId입니다");

        }
        return user.getNickname();
    }
}
