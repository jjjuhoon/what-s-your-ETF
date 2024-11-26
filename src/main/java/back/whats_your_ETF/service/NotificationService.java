package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.PortfolioResponse;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.repository.EmitterRepository;
import back.whats_your_ETF.repository.PortfolioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분
    private final EmitterRepository emitterRepository;
    private final PortfolioRepository portfolioRepository;
    private final EtfService etfService;

    // SSE 연결 설정
    public SseEmitter subscribe(Long userId) {
        String id = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        emitter.onTimeout(() -> emitterRepository.deleteById(id));

        return emitter;
    }

    // Portfolio 알림 설정 추가
    public void addPortfolioNotification(Long userId, Long portfolioId) {
        List<Long> existingPortfolioIds = emitterRepository.getUserPortfolioPreferences(userId);

        if (existingPortfolioIds.size() >= 3) {
            throw new IllegalArgumentException("알림 설정은 최대 3개의 Portfolio만 가능합니다.");
        }

        if (!existingPortfolioIds.contains(portfolioId)) {
            existingPortfolioIds.add(portfolioId);
            emitterRepository.saveUserPortfolioPreferences(userId, existingPortfolioIds);
        }
    }

    // Portfolio 수익률 변동 알림
    @Scheduled(fixedRate = 5000) // 5초마다 실행
    @Transactional
    public void monitorPortfolioChanges() {
        List<Portfolio> portfolios = portfolioRepository.findAll();

        for (Portfolio portfolio : portfolios) {
            double newRevenue = etfService.calculatePortfolioRevenuePercentage(portfolio);

            // 수익률 변동 감지
            if (emitterRepository.isRevenueChanged(portfolio.getId(), newRevenue)) {
                // DB 업데이트
                portfolio.setRevenue(newRevenue);
                portfolioRepository.save(portfolio);

                PortfolioResponse response = new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        newRevenue,
                        portfolio.getInvestAmount()
                );
                sendNotification(portfolio.getUser().getId(), response);
                emitterRepository.updateRevenueCache(portfolio.getId(), newRevenue);
            }
        }
    }

    private void sendNotification(Long userId, PortfolioResponse response) {
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(String.valueOf(userId));
        sseEmitters.forEach((key, emitter) -> sendToClient(emitter, key, response));
    }

    private void sendToClient(SseEmitter emitter, String id, PortfolioResponse response) {
        try {
            emitter.send(SseEmitter.event().id(id).name("portfolio-update").data(response));
        } catch (IOException exception) {
            emitterRepository.deleteById(id);
        }
    }



}
