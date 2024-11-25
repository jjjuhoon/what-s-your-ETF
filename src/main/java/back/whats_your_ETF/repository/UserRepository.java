package back.whats_your_ETF.repository;

import back.whats_your_ETF.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByNickname(String nickname); // 닉네임 검색
}
