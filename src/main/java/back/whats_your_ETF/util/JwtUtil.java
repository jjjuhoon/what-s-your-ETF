package back.whats_your_ETF.util;

import back.whats_your_ETF.dto.TokenPair;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
/*
    1. 고정된 SECRET_KEY를 이용할 경우
    private final String SECRET = "your_fixed_secret_key_here_your_fixed_secret_key_here";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes()); // 고정된 키 생성
*/

/*    2. 외부(환경변수)에서 가져올 경우
    private final SecretKey SECRET_KEY;

    public JwtUtil() {
        String secret = System.getenv("JWT_SECRET"); // 환경 변수에서 비밀키 가져오기
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable is not set.");
        }
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }
*/
    // 3. 랜덤 (서버 재실행 시 매번 랜덤)
private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 비밀키 생성
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
    // Access Token 생성
    public String generateAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Access Token과 Refresh Token을 함께 생성
    public TokenPair generateToken(String userId) {
        String accessToken = generateAccessToken(userId);
        String refreshToken = generateRefreshToken(userId);
        return new TokenPair(accessToken, refreshToken);
    }


    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}
