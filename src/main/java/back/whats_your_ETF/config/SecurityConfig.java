package back.whats_your_ETF.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 -> JWT토큰 기반을 사용하는 경우 세션을 사용하지 않을 떄는 비활성화해도 무방.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("api/auth/**").permitAll() //api 엔드포인트 허용
                        .requestMatchers(
                                "/swagger-ui/**",  // Swagger UI
                                "/v3/api-docs/**", // OpenAPI Docs
                                "/swagger-resources/**", // Swagger 관련 리소스
                                "/webjars/**"      // Swagger UI에 필요한 웹 자원
                        ).permitAll() // Swagger 관련 엔드포인트는 인증 없이 접근 가능
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable); // 필요하면 formLogin 설정 가능

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
