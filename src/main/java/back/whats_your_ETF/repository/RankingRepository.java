package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByStockCode(String stockCode);
    Optional<Ranking> findByStockName(String stockName);

    @Query("SELECT r.currentPrice FROM Ranking r WHERE r.stockCode = :stockCode")
    Optional<Long> findCurrentPriceByStockCode(@Param("stockCode") String stockCode);


    @Query("SELECT r FROM Ranking r WHERE r.fluctuationRank IS NOT NULL ORDER BY r.fluctuationRank ASC")
    List<Ranking> findTop30ByFluctuationRank();

    @Query("SELECT r FROM Ranking r WHERE r.marketCapRank IS NOT NULL ORDER BY r.marketCapRank ASC")
    List<Ranking> findTop30ByMarketCapRank();

    @Query("SELECT r FROM Ranking r WHERE r.profitAssetIndexRank IS NOT NULL ORDER BY r.profitAssetIndexRank ASC")
    List<Ranking> findTop30ByProfitAssetIndexRank();

    @Query("SELECT r FROM Ranking r WHERE r.volumeRank IS NOT NULL ORDER BY r.volumeRank ASC")
    List<Ranking> findTop30ByVolumeRank();

}
