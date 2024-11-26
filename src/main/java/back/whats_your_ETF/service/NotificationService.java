package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.NoticeResponse;
import back.whats_your_ETF.dto.PortfolioResponse;
import back.whats_your_ETF.entity.Notice;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.repository.EmitterRepository;
import back.whats_your_ETF.repository.NoticeRepository;
import back.whats_your_ETF.repository.PortfolioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.embedded.netty.NettyWebServer;
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
    private final NoticeRepository noticeRepository;
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

    // 매일 자정에 alreadySend 초기화
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetAlreadySendFlags() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        for (Portfolio portfolio : portfolios) {
            portfolio.setAlreadySend(false);
        }
        portfolioRepository.saveAll(portfolios);
        System.out.println("알림 전송 플래그 초기화 완료");
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

                // PortfolioResponse 생성 및 SSE 전송
                PortfolioResponse response = new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        newRevenue,
                        portfolio.getInvestAmount()
                );
                sendPortfolioUpdateNotification(portfolio.getUser().getId(), response);
                emitterRepository.updateRevenueCache(portfolio.getId(), newRevenue);
            }
            // 익절/손절 Spot 알림
            if ((newRevenue >= portfolio.getProfitSpot() || newRevenue <= portfolio.getLossSpot()) && !portfolio.isAlreadySend()) {
                sendProfitLossSpotNotification(portfolio, newRevenue);
            }
        }
    }

    private void sendNotification(Long userId, PortfolioResponse response) {
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(String.valueOf(userId));
        sseEmitters.forEach((key, emitter) -> sendToClient(emitter, key, response));
    }

    //실시간 portfolio수익률 변동알림
    private void sendPortfolioUpdateNotification(Long userId, PortfolioResponse response) {
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(String.valueOf(userId));
        sseEmitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event().id(key).name("portfolio-update").data(response));
            } catch (IOException exception) {
                emitterRepository.deleteById(key);
            }
        });
    }

    //손익,손절spot 알림 생성
    private void sendProfitLossSpotNotification(Portfolio portfolio, double newRevenue) {
        String message = newRevenue >= portfolio.getProfitSpot()
                ? "수익률이 목표치에 도달했습니다!"
                : "손실률이 설정한 한도에 도달했습니다!";

        // Notice 저장
        Notice notice = Notice.builder()
                .user(portfolio.getUser())
                .content("[" + portfolio.getTitle() + "] " + message)
                .isRead(false)
                .build();
        noticeRepository.save(notice);

        // NoticeResponse 생성 및 SSE 전송
        NoticeResponse response = new NoticeResponse(
                notice.getId(),
                notice.getContent(),
                notice.isRead(),
                notice.getUser().getId()
        );
        sendNotice(portfolio.getUser().getId(), response);
//        portfolio.setAlreadySend(true);
    }
    //익절,손절 알림전송
    private void sendNotice(Long userId, NoticeResponse response) {
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(String.valueOf(userId));
        sseEmitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event().id(key).name("notice").data(new ObjectMapper().writeValueAsString(response)));
            } catch (IOException exception) {
                emitterRepository.deleteById(key);
            }
        });
    }



    private void sendToClient(SseEmitter emitter, String id, PortfolioResponse response) {
        try {
            emitter.send(SseEmitter.event().id(id).name("portfolio-update").data(response));
        } catch (IOException exception) {
            emitterRepository.deleteById(id);
        }
    }



}
