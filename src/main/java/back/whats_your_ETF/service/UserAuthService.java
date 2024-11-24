package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.TokenPair;
import back.whats_your_ETF.entity.RefreshToken;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.RefreshTokenRepository;
import back.whats_your_ETF.repository.UserRepository;
import back.whats_your_ETF.util.JwtUtil;
import back.whats_your_ETF.util.RedisUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    private static final long REFRESH_TOKEN_TTL = 60 * 60 * 24 * 7; // 7일

    // 회원가입
    public void signup(SignUpRequest signUpRequest) {
        if (userRepository.findByUserId(signUpRequest.userId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 ID입니다.");
        }

        User user = User.builder()
                .userId(signUpRequest.userId())
                .password(passwordEncoder.encode(signUpRequest.password()))
                .nickname(signUpRequest.nickname())
                .isInTop10(false)
                .level(0L)
                .member(false)
                .build();

        userRepository.save(user);
    }

    public UserAuthService(JwtUtil jwtUtil, UserRepository userRepository,
                           RedisUtil redisUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.redisUtil = redisUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // RefreshToken 저장 또는 갱신
    private void saveOrUpdateRefreshToken(String userId, String refreshToken) {
        redisUtil.save(userId, refreshToken, REFRESH_TOKEN_TTL);
    }

    // RefreshToken 검증
    private boolean validateRefreshToken(String userId, String refreshToken) {
        String storedToken = redisUtil.get(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    // 사용자 인증 및 토큰 발급
    public TokenPair authenticate(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        TokenPair tokenPair = jwtUtil.generateToken(userId);
        saveOrUpdateRefreshToken(userId, tokenPair.getRefreshToken());
        return tokenPair;
    }

    // AccessToken 갱신
    public TokenPair refreshAccessToken(String refreshToken) {
        String userId = jwtUtil.extractUserId(refreshToken);

        if (!validateRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 RefreshToken입니다.");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        return new TokenPair(newAccessToken, refreshToken);
    }

}
