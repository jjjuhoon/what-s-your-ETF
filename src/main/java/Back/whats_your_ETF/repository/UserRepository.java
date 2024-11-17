package Back.whats_your_ETF.repository;

import Back.whats_your_ETF.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
