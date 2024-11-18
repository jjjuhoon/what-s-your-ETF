package Back.whats_your_ETF.repository;

import Back.whats_your_ETF.entity.ETFStock;
import Back.whats_your_ETF.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStockCode(String stockCode);

}
