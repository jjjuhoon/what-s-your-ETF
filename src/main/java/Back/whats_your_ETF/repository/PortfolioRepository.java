package Back.whats_your_ETF.repository;

import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);
    Optional<Portfolio> findByUserAndIsEtfTrue(User user);
}
