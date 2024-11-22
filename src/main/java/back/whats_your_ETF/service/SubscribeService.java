package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.SubscribeResponse;
import back.whats_your_ETF.entity.Subscribe;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.SubscribeRepository;
import back.whats_your_ETF.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;

    //구독 목록 조회
    public Optional<List<SubscribeResponse>> getSubscriptionsByUserId(Long userId) {

        List<Subscribe> subscriptions = subscribeRepository.findAllBySubscriberId(userId);

        if (subscriptions.isEmpty()) {
            return Optional.empty();
        }

        List<SubscribeResponse> responses = subscriptions.stream()
                .map(subscribe -> new SubscribeResponse(
                        subscribe.getPublisher().getId(),
                        subscribe.getPublisher().getNickname(),
                        subscribe.getPublisher().getAsset() // revenue를 asset로 간주
                ))
                .collect(Collectors.toList());

        return Optional.of(responses);
    }

    // 구독하기
    @Transactional
    public void subscribe(Long subscriberId, Long publisherId) {
        if (subscribeRepository.existsBySubscriberIdAndPublisherId(subscriberId, publisherId)) {
            throw new IllegalStateException("이미 구독된 사용자입니다.");
        }

        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new IllegalArgumentException("구독자를 찾을 수 없습니다."));
        User publisher = userRepository.findById(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("퍼블리셔를 찾을 수 없습니다."));

        Subscribe subscribe = Subscribe.builder()
                .subscriber(subscriber)
                .publisher(publisher)
                .build();

        subscribeRepository.save(subscribe);

        publisher.setSubscriberCount(publisher.getSubscriberCount() + 1);
        userRepository.save(publisher);
    }

    // 구독 취소하기
    @Transactional
    public void unsubscribe(Long subscriberId, Long publisherId) {
        if (!subscribeRepository.existsBySubscriberIdAndPublisherId(subscriberId, publisherId)) {
            throw new IllegalStateException("구독하지 않은 사용자입니다.");
        }

        subscribeRepository.deleteBySubscriberIdAndPublisherId(subscriberId, publisherId);

        User publisher = userRepository.findById(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("퍼블리셔를 찾을 수 없습니다."));

        publisher.setSubscriberCount(publisher.getSubscriberCount() - 1);
        userRepository.save(publisher);
    }
}
