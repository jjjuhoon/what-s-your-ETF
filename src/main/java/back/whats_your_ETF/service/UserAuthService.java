package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.SignUpRequest;
import back.whats_your_ETF.dto.TokenPair;
import back.whats_your_ETF.entity.RefreshToken;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.RefreshTokenRepository;
import back.whats_your_ETF.repository.UserRepository;
import back.whats_your_ETF.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthService(JwtUtil jwtUtil, UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
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

        // AccessToken과 RefreshToken 생성
        TokenPair tokenPair = jwtUtil.generateToken(userId);

//         RefreshToken 저장 (DB에 저장하거나 갱신)
        saveOrUpdateRefreshToken(user, tokenPair.getRefreshToken());

        return tokenPair;
    }

    // AccessToken 갱신
    public TokenPair refreshAccessToken(String refreshToken) {
        String userId = jwtUtil.extractUserId(refreshToken);

        // RefreshToken 유효성 검증
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 RefreshToken입니다."));

        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken이 일치하지 않습니다.");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
        }

        // 새로운 AccessToken과 기존 RefreshToken 반환
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        return new TokenPair(newAccessToken, refreshToken);
    }

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

    // RefreshToken 저장 또는 갱신
    private void saveOrUpdateRefreshToken(User user, String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getUserId())
                .orElse(new RefreshToken());
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setRefreshToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
