package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import back.whats_your_ETF.entity.User;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT r FROM RefreshToken r WHERE r.user.userId = :userId")
    Optional<RefreshToken> findByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
    void deleteByUser(@Param("user") User user);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
