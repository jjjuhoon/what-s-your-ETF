package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.ETFStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ETFStockRepository extends JpaRepository<ETFStock, Long> {

    @Query("SELECT e FROM ETFStock e WHERE e.portfolio.id = :portfolioId")
    List<ETFStock> findAllByPortfolioId(@Param("portfolioId") Long portfolioId);
}
