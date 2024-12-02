package back.whats_your_ETF.service;


import back.whats_your_ETF.apiPayload.GeneralException;
import back.whats_your_ETF.apiPayload.code.status.ErrorStatus;
import back.whats_your_ETF.dto.*;
import back.whats_your_ETF.entity.ETFStock;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.*;

import back.whats_your_ETF.apiPayload.GeneralException;
import back.whats_your_ETF.apiPayload.code.status.ErrorStatus;
import back.whats_your_ETF.dto.*;
import back.whats_your_ETF.entity.ETFStock;
import back.whats_your_ETF.entity.Portfolio;
import back.whats_your_ETF.entity.User;
import back.whats_your_ETF.repository.ETFStockRepository;
import back.whats_your_ETF.repository.PortfolioRepository;
import back.whats_your_ETF.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtfService {

    private final ETFStockRepository etfStockRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final RankingRepository rankingRepository;
    private final RedisTemplate<String, String> redisTemplate;



    @Transactional
    public void buyETF(Long userId, EtfRequest.etfInvestList etfInvestList) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 2. 새로운 포트폴리오 생성
        Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
                .user(user)
                .title(etfInvestList.getTitle())
                .isEtf(true)
                .build());

        // 3. 총 투자 금액 계산
        long totalInvestment = etfInvestList.getEtfList().stream()
                .mapToLong(EtfRequest.etfInvest::getPrice)
                .sum();

        // 4. 투자 금액만큼 사용자 자산 감소
        if (user.getAsset() < totalInvestment) {
            throw new GeneralException(ErrorStatus.INSUFFICIENT_FUNDS); // 자산 부족 예외 처리
        }
        user.setAsset(user.getAsset() - totalInvestment);
        userRepository.save(user); // 사용자 자산 업데이트

        // 5. 포트폴리오의 투자 금액 업데이트
        portfolio.setInvestAmount(totalInvestment);
        portfolioRepository.save(portfolio);

        // 6. Redis에서 데이터 조회 후 ETFStock 생성
        etfInvestList.getEtfList().forEach(etfInvest -> {
            String redisKey = "stock:" + etfInvest.getStockName();

            // Redis에서 각 필드 가져오기 (Object를 String으로 캐스팅)
            String stockName = (String) redisTemplate.opsForHash().get(redisKey, "stockName");
            String stockCode = (String) redisTemplate.opsForHash().get(redisKey, "stockCode");
            String priceString = (String) redisTemplate.opsForHash().get(redisKey, "price");

            if (stockName == null || stockCode == null || priceString == null) {
                throw new GeneralException(ErrorStatus.STOCK_NOT_FOUND); // Redis에서 해당 Stock을 찾을 수 없음
            }

            Long price = Long.valueOf(priceString);

            // ETFStock 엔티티 생성 및 저장
            ETFStock etfStock = ETFStock.builder()
                    .portfolio(portfolio)
                    .stockName(stockName)           // Redis에서 가져온 이름
                    .stockCode(stockCode)           // Redis에서 가져온 코드
                    .percentage(etfInvest.getPercentage()) // 요청에서 받은 퍼센트
                    .purchasePrice(price)           // Redis에서 가져온 가격
                    .build();

            etfStockRepository.save(etfStock);
        });
    }






    @Transactional
    public void sellETF(Long portfolioId) {
        // 1. 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PORTFOLIO_NOT_FOUND));

        User user = portfolio.getUser();

        // 2. 수익금 계산: 투자금 * (수익률 / 100)
        long investAmount = portfolio.getInvestAmount();
        double revenuePercentage = portfolio.getRevenue() != null ? portfolio.getRevenue() : 0;
        long profit = (long) (investAmount * (revenuePercentage / 100.0));

        // 3. 사용자 자산 업데이트 (투자금 + 수익금 반환)
        long totalRefund = investAmount + profit;
        user.setAsset(user.getAsset() + totalRefund);
        userRepository.save(user);

        // 4. 포트폴리오에 속한 모든 ETFStock 삭제
        etfStockRepository.deleteAll(portfolio.getEtfStocks());

        // 5. 포트폴리오 삭제
        portfolioRepository.delete(portfolio);
    }


    public Optional<PortfolioDetailsResponse> getPortfolioDetailsByPortfolioId(Long portfolioId) {

        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(portfolioId);

        if (portfolioOptional.isEmpty()) {
            return Optional.empty();
        }

        Portfolio portfolio = portfolioOptional.get();

        List<ETFStock> etfStocks = etfStockRepository.findAllByPortfolioId(portfolioId);

        List<ETFStockResponse> etfStockResponses = etfStocks.stream()
                .map(etfStock -> new ETFStockResponse(
                        etfStock.getStockCode(),
                        etfStock.getStockName(),
                        etfStock.getPercentage(),
                        etfStock.getPurchasePrice()
                ))
                .collect(Collectors.toList());

        PortfolioDetailsResponse response = new PortfolioDetailsResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getRevenue(),
                portfolio.getInvestAmount(),
                etfStockResponses
        );

        return Optional.of(response);
    }

    //2.1.1 포트폴리오 랭킹별로 가져오기
    public PortfolioListResponse getPortfolioRank() {
        List<Portfolio> portfolios = portfolioRepository.findAllOrderByRevenueDesc();

        List<PortfolioResponse> portfolioResponses = portfolios.stream()
                .map(portfolio -> new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        portfolio.getRevenue(),
                        portfolio.getInvestAmount()
                ))
                .collect(Collectors.toList());

        return new PortfolioListResponse(portfolioResponses);
    }

    //2.1.2 : 수익률 높은순으로 유저 랭킹 (Portfolio 수익률 계산 메서드)
    // Portfolio 수익률 계산 메서드
    public double calculatePortfolioRevenuePercentage(Portfolio portfolio) {
        // ETFStocks에서 수익률 계산
        double totalRevenue = portfolio.getEtfStocks().stream()
                .mapToDouble(etfStock -> {
                    // Redis에서 해당 Stock 데이터를 조회
                    String redisKey = "stock:" + etfStock.getStockName();
                    Map<Object, Object> stockData = redisTemplate.opsForHash().entries(redisKey);

                    // 현재 가격 조회 (Redis에 없을 경우 구매 가격 사용)
                    Long currentPrice = stockData.containsKey("price")
                            ? Long.valueOf((String) stockData.get("price"))
                            : etfStock.getPurchasePrice();

                    // 수익 계산
                    return (currentPrice - etfStock.getPurchasePrice()) * etfStock.getPercentage();
                })
                .sum();

        // 총 투자 금액으로 나눠 수익률 계산
        return (totalRevenue / portfolio.getInvestAmount()) * 100;
    }



    //2.1.2 : 수익률 높은순으로 유저 랭킹 (Portfolio 수익률 계산 메서드)
    // Portfolio 수익금 계산 메서드
    public double calculatePortfolioRevenue(Portfolio portfolio) {
        return portfolio.getEtfStocks().stream()
                .mapToDouble(etfStock -> {
                    String redisKey = "stock:" + etfStock.getStockName();
                    Map<Object, Object> stockData = redisTemplate.opsForHash().entries(redisKey);

                    // Redis에서 현재 가격 가져오기
                    Long currentPrice = stockData.containsKey("price")
                            ? Long.valueOf((String) stockData.get("price"))
                            : etfStock.getPurchasePrice();

                    return (currentPrice - etfStock.getPurchasePrice()) * etfStock.getPercentage();
                })
                .sum();
    }



    public Optional<Double> getUserRevenuePercentage(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Optional.empty();
        }

        double revenuePercentage = calculateUserRevenuePercentage(user);
        double roundedRevenuePercentage = BigDecimal.valueOf(revenuePercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return Optional.of(roundedRevenuePercentage);
    }



    // 유저 한 명의 revenuePercentage 계산 메서드
    public double calculateUserRevenuePercentage(User user) {
        List<Portfolio> portfolios = user.getPortfolioss();

        long totalInvestAmount = portfolios.stream()
                .mapToLong(Portfolio::getInvestAmount)
                .sum();

        double totalRevenue = portfolios.stream()
                .mapToDouble(this::calculatePortfolioRevenue)
                .sum();

        return (totalInvestAmount > 0)
                ? (totalRevenue / totalInvestAmount) * 100
                : 0.0;
    }

    //2.1.2 : 수익률 높은순으로 유저 랭킹
    public List<UserRankingResponse> getUserRanking() {

        List<User> users = portfolioRepository.findAllUsersWithPortfolios();

        List<UserRankingResponse> userRankings = users.stream()
                .map(user -> {
                    double revenuePercentage = calculateUserRevenuePercentage(user);

                    double totalRevenue = user.getPortfolioss().stream()
                            .mapToDouble(this::calculatePortfolioRevenue)
                            .sum();

                    return new UserRankingResponse(
                            user.getId(),
                            user.getNickname(),
                            user.getImage(),
                            totalRevenue,
                            revenuePercentage
                    );
                })
                .sorted(Comparator.comparingDouble(UserRankingResponse::revenuePercentage).reversed())
                .collect(Collectors.toList());

        return userRankings;
    }

}
