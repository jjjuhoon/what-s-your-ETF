package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT r FROM RefreshToken r WHERE r.user.userId = :userId")
    Optional<RefreshToken> findByUserId(@Param("userId") String userId);
}
