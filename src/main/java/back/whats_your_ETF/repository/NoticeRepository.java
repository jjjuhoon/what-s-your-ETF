package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByUserIdAndIsReadFalse(Long userId);
}

