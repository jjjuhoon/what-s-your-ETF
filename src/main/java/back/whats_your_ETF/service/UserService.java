package back.whats_your_ETF.service;

import back.whats_your_ETF.apiPayload.GeneralException;
import back.whats_your_ETF.apiPayload.code.status.ErrorStatus;
import back.whats_your_ETF.dto.*;
import back.whats_your_ETF.entity.ETFStock;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.entity.Ranking;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.ETFStockRepository;
import back.whats_your_ETF.repository.PortfolioRepository;
import back.whats_your_ETF.repository.RankingRepository;
import back.whats_your_ETF.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final ETFStockRepository etfStockRepository;
    private final EtfService etfService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RankingRepository rankingRepository;


    // 특정 ID로 사용자 정보 조회
    public Optional<UserResponse> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getLevel(),
                        user.getImage(),
                        user.getMember(),
                        user.getAsset(),
                        user.getSubscriberCount(),
                        etfService.calculateUserRevenuePercentage(user)
                ));
    }

    // 1.1.2 멤버십 가입하기
    @Transactional
    public ResponseEntity<String> updateMembership(Long userId) {
        // 1. 사용자 조회
        Optional<User> optionalUser = userRepository.findById(userId);

        // 2. 사용자 존재 여부 확인
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build(); // 사용자 없음
        }

        User user = optionalUser.get();

        // 3. 이미 멤버십 가입된 경우
        if (Boolean.TRUE.equals(user.getMember())) {
            return ResponseEntity.badRequest().body("이미 멤버십에 가입된 사용자입니다.");
        }

        // 4. 멤버십 가입 처리
        user.setMember(true);
        userRepository.save(user); // 업데이트된 상태 저장

        return ResponseEntity.ok("회원 상태로 업데이트되었습니다.");
    }

    //1.1.3 멤버십 해지하기
    @Transactional
    public ResponseEntity<String> deleteMembership(Long userId) {

        //사용자 조회
        Optional<User> optionalUser = userRepository.findById(userId);

        //사용자 존재 여부 확인
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
            //사용자 없음
        }
        User user = optionalUser.get();

        //멤버십에 가입되지 않은 경우
        if (Boolean.FALSE.equals(user.getMember())) {
            return ResponseEntity.badRequest().body("멤버십에 가입되지 않은 사용자입니다.");
        }

        //멤버십 해지 처리
        user.setMember(false);
        userRepository.save(user);

        return ResponseEntity.ok("해지가 완료되었습니다.");


    }



    // 1.2.1 : 나의 거래내역 가져오기
    public Optional<List<TradeHistoryResponse>> getTradeHistoryById(Long userId) {

        List<Portfolio> portfolios = portfolioRepository.findByUserIdAndIsEtf(userId);

        if (portfolios.isEmpty()) {
            return Optional.empty();
        }

        // Portfolio 최신순 정렬
        List<TradeHistoryResponse> tradeHistoryResponses = portfolios.stream()
                .sorted(Comparator.comparing(Portfolio::getCreatedAt).reversed())
                .map(portfolio -> {
                    List<ETFStock> etfStocks = etfStockRepository.findAllByPortfolioId(portfolio.getId());

                    // ETFStock 최신순 정렬
                    List<StockResponse> stockResponses = etfStocks.stream()
                            .sorted(Comparator.comparing(ETFStock::getCreatedAt).reversed())
                            .map(etfStock -> new StockResponse(
                                    etfStock.getStockCode(),
                                    etfStock.getStockName(),
                                    etfStock.getPercentage()
                            ))
                            .collect(Collectors.toList());

                    return new TradeHistoryResponse(
                            portfolio.getId(),
                            portfolio.getTitle(),
                            portfolio.getRevenue(),
                            portfolio.getInvestAmount(),
                            stockResponses
                    );
                })
                .collect(Collectors.toList());

        return Optional.of(tradeHistoryResponses);
    }

    //1.2.2 나의 분석 리포트 가져오기 (가장 수익률이 높은 포트폴리오 가져오기)
    public Optional<MyReportResponse> getMyReport(Long userId) {
        // User의 포트폴리오 중 ETF만 가져오기
        List<Portfolio> portfolios = portfolioRepository.findByUserIdAndIsEtf(userId);

        if (portfolios.isEmpty()) {
            return Optional.empty();
        }

        // 가장 수익률이 높은 포트폴리오 찾기 (동률인 경우 최신 순으로)
        Portfolio topPortfolio = portfolios.stream()
                .max(Comparator.comparingDouble(Portfolio::getRevenue)
                        .thenComparing(Portfolio::getCreatedAt)) // 생성일 기준 정렬 추가
                .orElseThrow(() -> new IllegalArgumentException("No portfolio found for user with id: " + userId));

        // 1. 포트폴리오에 해당하는 총 투자금액 가져오기
        double totalInvestAmount = topPortfolio.getInvestAmount();

        // 2. 종목별 수익률 * 종목별 비중 계산
        List<MyReportResponse.StockPerformance> stockPerformances = topPortfolio.getEtfStocks().stream()
                .map(etfStock -> {
                    String redisKey = "stock:" + etfStock.getStockName();

                    // Redis에서 종목 데이터 가져오기
                    String stockName = (String) redisTemplate.opsForHash().get(redisKey, "stockName");
                    String stockCode = (String) redisTemplate.opsForHash().get(redisKey, "stockCode");
                    String priceString = (String) redisTemplate.opsForHash().get(redisKey, "price");

                    // Redis에 데이터가 없는 경우 DB에서 가져오기
                    if (stockName == null || stockCode == null || priceString == null) {
                        Ranking ranking = rankingRepository.findByStockName(etfStock.getStockName())
                                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

                        stockName = ranking.getStockName();
                        stockCode = ranking.getStockCode();
                        priceString = String.valueOf(ranking.getCurrentPrice());
                    }

                    Long currentPrice = Long.valueOf(priceString);

                    // 종목별 수익률 계산
                    double stockYield = ((currentPrice - etfStock.getPurchasePrice()) / (double) etfStock.getPurchasePrice()) * 100;

                    // 종목별 비중 계산
                    double stockWeight = (etfStock.getPercentage() * totalInvestAmount) / totalInvestAmount;

                    return new MyReportResponse.StockPerformance(stockName, stockYield * stockWeight);
                })
                // 가중 수익률 기준으로 정렬
                .sorted((s1, s2) -> Double.compare(s2.weightedYield(), s1.weightedYield()))
                .collect(Collectors.toList());

        // MyReportResponse DTO로 변환
        MyReportResponse myReportResponse = new MyReportResponse(
                topPortfolio.getUser().getNickname(),
                topPortfolio.getTitle(),
                topPortfolio.getRevenue(),
                stockPerformances
        );

        return Optional.of(myReportResponse);
    }



    // 1.3.1 : 나의 ETF목록 가져오기
    public Optional<PortfolioListResponse> getUserETFlistById(Long userId) {

        List<Portfolio> portfolios = portfolioRepository.findByUserIdAndIsEtf(userId);

        if (portfolios.isEmpty()) {
            return Optional.empty();
        }

        List<PortfolioResponse> portfolioResponses = portfolios.stream()
                .map(portfolio -> new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        portfolio.getRevenue(),
                        portfolio.getInvestAmount()
                ))
                .collect(Collectors.toList());

        PortfolioListResponse etflistResponse = new PortfolioListResponse(portfolioResponses);

        return Optional.of(etflistResponse);
    }



}
