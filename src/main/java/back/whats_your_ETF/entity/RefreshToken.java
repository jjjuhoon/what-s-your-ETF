package back.whats_your_ETF.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 기본 키

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user; // User와 1:1 연관 관계

    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken; // Refresh Token 값
}
