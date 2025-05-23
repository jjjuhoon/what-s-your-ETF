package back.whats_your_ETF.service;

import back.whats_your_ETF.dto.NoticeResponse;
import back.whats_your_ETF.dto.PortfolioNotificationDeleteRequest;
import back.whats_your_ETF.dto.PortfolioNotificationSingleRequest;
import back.whats_your_ETF.dto.PortfolioResponse;
import back.whats_your_ETF.entity.Notice;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.repository.EmitterRepository;
import back.whats_your_ETF.repository.NoticeRepository;
import back.whats_your_ETF.repository.PortfolioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void addPortfolioNotification(PortfolioNotificationSingleRequest request) {
        Long userId= request.userId();
        Long portfolioId= request.portfolioId();
        Long profitSpot=request.profitSpot();
        Long lossSpot=request.lossSpot();
        List<Long> existingPortfolioIds = emitterRepository.getUserPortfolioPreferences(userId);
        System.out.println("구독중인 알림갯수:"+existingPortfolioIds.size());
        if (existingPortfolioIds.size() >= 3) {
            throw new IllegalArgumentException("알림 설정은 최대 3개만 가능합니다");
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Portfolio를 찾을 수 없습니다."));

        portfolio.setProfitSpot(profitSpot);
        portfolio.setLossSpot(lossSpot);

        portfolioRepository.save(portfolio);

        if (!existingPortfolioIds.contains(portfolioId)) {
            existingPortfolioIds.add(portfolioId);
            emitterRepository.saveUserPortfolioPreferences(userId, existingPortfolioIds);
        }
    }

    // Portfolio 알림 삭제 서비스
    public void deletePortfolioNotification(PortfolioNotificationDeleteRequest request) {
        Long userId = request.userId();
        Long portfolioId = request.portfolioId();

        List<Long> existingPortfolioIds = emitterRepository.getUserPortfolioPreferences(userId);

        if (!existingPortfolioIds.contains(portfolioId)) {
            throw new IllegalArgumentException("해당 Portfolio는 알림 설정에 포함되어 있지 않습니다.");
        }

        existingPortfolioIds.remove(portfolioId);
        emitterRepository.saveUserPortfolioPreferences(userId, existingPortfolioIds);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Portfolio를 찾을 수 없습니다."));

        portfolio.setProfitSpot(null);
        portfolio.setLossSpot(null);

        portfolioRepository.save(portfolio);
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
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void monitorPortfolioChanges() {
        List<Portfolio> portfolios = portfolioRepository.findAll();

        for (Portfolio portfolio : portfolios) {
            try {
                double newRevenue = etfService.calculatePortfolioRevenuePercentage(portfolio);
//                System.out.println("Portfolio ID: " + portfolio.getId() + ", New Revenue: " + newRevenue);

                if (emitterRepository.isRevenueChanged(portfolio.getId(), newRevenue)) {
                    portfolio.setRevenue(newRevenue);
                    portfolioRepository.save(portfolio);

                    PortfolioResponse response = new PortfolioResponse(
                            portfolio.getId(),
                            portfolio.getTitle(),
                            newRevenue,
                            portfolio.getInvestAmount()
                    );
                    sendPortfolioUpdateNotification(portfolio.getUser().getId(), response);
                    emitterRepository.updateRevenueCache(portfolio.getId(), newRevenue);
                }

                // 익절/손절 알림 처리
                if ((newRevenue >= portfolio.getProfitSpot() || newRevenue <= portfolio.getLossSpot()) && !portfolio.isAlreadySend()) {
                    sendProfitLossSpotNotification(portfolio, newRevenue);
                }
            } catch (Exception e) {
                System.err.println("Error while monitoring portfolio changes: " + e.getMessage());
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

    private void sendProfitLossSpotNotification(Portfolio portfolio, double newRevenue) {
        // 구독 여부 확인
        Long userId = portfolio.getUser().getId();
        List<Long> subscribedPortfolios = emitterRepository.getUserPortfolioPreferences(userId);

        System.out.println("구독된 포트폴리오: " + subscribedPortfolios);

        if (!subscribedPortfolios.contains(portfolio.getId())) {
            // 사용자가 해당 Portfolio를 구독하지 않았으면 알림 전송하지 않음
            System.out.println("사용자가 포트폴리오를 구독하지 않았습니다. 알림 전송 생략.");
            return;
        }

        // 알림 메시지 생성
        String message = newRevenue >= portfolio.getProfitSpot()
                ? "수익률 익절 알림"
                : "손실률 손절 알림";

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

        // sendNotice에 구독 여부를 확인한 후 전송
        sendNotice(userId, response);

        // 알림 중복 방지 플래그 설정
//        portfolio.setAlreadySend(true);
//        portfolioRepository.save(portfolio);
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
