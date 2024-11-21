package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    //구독자 목록 가져오기
    @Query("SELECT s FROM Subscribe s JOIN FETCH s.publisher p WHERE s.subscriber.id = :userId")
    List<Subscribe> findAllBySubscriberId(Long userId);

    //구독자 삭제하기
    void deleteBySubscriberIdAndPublisherId(Long subscriberId, Long publisherId);

    //구독목록에 있는지 확인
    boolean existsBySubscriberIdAndPublisherId(Long subscriberId, Long publisherId);
}
