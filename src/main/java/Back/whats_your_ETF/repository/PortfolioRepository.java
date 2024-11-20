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

    Optional<Portfolio> findById(Long portfolioId);

    //내가 매도한 portfolio만 보여줄때
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.isEtf = true")
    List<Portfolio> findByUserIdAndIsEtf(@Param("userId") Long userId);

    //포트폴리오 수익률별로 가져오기
    @Query("SELECT p FROM Portfolio p ORDER BY p.revenue DESC")
    List<Portfolio> findAllOrderByRevenueDesc();

    //유저와 포트폴리오를 함께 가져오기
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.portfolioss")
    List<User> findAllUsersWithPortfolios();
}
