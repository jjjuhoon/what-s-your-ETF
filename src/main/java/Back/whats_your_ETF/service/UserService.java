package Back.whats_your_ETF.service;

import Back.whats_your_ETF.entity.User;
import Back.whats_your_ETF.repository.UserRepository;
import jakarta.activation.MimeTypeParameterList;
import jakarta.transaction.Transactional;
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

    //membership update
    @Transactional
    public boolean updateMembership(Long userId){
        Optional<User> optionalUser = userRepository.findById(userId);
        
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setMember(true); // 회원 상태를 true로 변경
            userRepository.save(user); // 변경된 데이터를 저장
            return true;
        }
        return false; //사용자 ID가 없을 경우
    }

}
