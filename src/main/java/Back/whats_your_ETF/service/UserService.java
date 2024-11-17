package Back.whats_your_ETF.service;

import Back.whats_your_ETF.entity.User;
import Back.whats_your_ETF.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 특정 ID로 사용자 정보 조회
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
}
