package Back.whats_your_ETF.repository;

import Back.whats_your_ETF.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    //구독자 목록 가져오기
    @Query("SELECT s FROM Subscribe s JOIN FETCH s.publisher p WHERE s.subscriber.id = :userId")
    List<Subscribe> findAllBySubscriberId(Long userId);
}
