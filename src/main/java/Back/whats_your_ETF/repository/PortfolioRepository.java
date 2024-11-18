package Back.whats_your_ETF.repository;

import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);


    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.isEtf = true")
    List<Portfolio> findByUserIdAndIsEtf(@Param("userId") Long userId);
}
