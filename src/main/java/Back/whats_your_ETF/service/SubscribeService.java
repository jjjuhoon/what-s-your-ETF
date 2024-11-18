package Back.whats_your_ETF.service;

import Back.whats_your_ETF.dto.SubscribeResponse;
import Back.whats_your_ETF.entity.Subscribe;
import Back.whats_your_ETF.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    public List<SubscribeResponse> getSubscriptionsByUserId(Long userId) {
        List<Subscribe> subscriptions = subscribeRepository.findAllBySubscriberId(userId);

        // Entity -> DTO 변환
        return subscriptions.stream()
                .map(subscribe -> new SubscribeResponse(
                        subscribe.getPublisher().getId(),
                        subscribe.getPublisher().getNickname(),
                        subscribe.getPublisher().getAsset() // revenue를 asset로 간주
                ))
                .collect(Collectors.toList());
    }
}
