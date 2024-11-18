package Back.whats_your_ETF.service;

import Back.whats_your_ETF.dto.StockResponse;
import Back.whats_your_ETF.dto.TradeHistoryResponse;
import Back.whats_your_ETF.dto.UserResponse;
import Back.whats_your_ETF.entity.ETFStock;
import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.repository.ETFStockRepository;
import Back.whats_your_ETF.repository.PortfolioRepository;
import Back.whats_your_ETF.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final ETFStockRepository etfStockRepository;

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
                        user.getSubscriberCount()
                ));
    }

    ////1.2.1 : 나의 거래내역 가져오기
    public Optional<List<TradeHistoryResponse>> getTradeHistoryById(Long userId) {

        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        if (portfolios.isEmpty()) {
            return Optional.empty();
        }

        //Portfolio 최신순 정렬
        List<TradeHistoryResponse> tradeHistoryResponses = portfolios.stream()
                .sorted(Comparator.comparing(Portfolio::getCreatedAt).reversed())
                .map(portfolio -> {
                    List<ETFStock> etfStocks = etfStockRepository.findAllByPortfolioId(portfolio.getId());

                    // ETFStock 최신순 정렬
                    List<StockResponse> stockResponses = etfStocks.stream()
                            .sorted(Comparator.comparing(ETFStock::getCreatedAt).reversed())
                            .map(etfStock -> StockResponse.builder()
                                    .stockCode(etfStock.getStock().getStockCode())
                                    .stockName(etfStock.getStock().getStockName())
                                    .percentage(etfStock.getPercentage())
                                    .build()
                            )
                            .collect(Collectors.toList());

                    return TradeHistoryResponse.builder()
                            .portfolioId(portfolio.getId())
                            .title(portfolio.getTitle())
                            .revenue(portfolio.getRevenue())
                            .investAmount(portfolio.getInvestAmount())
                            .stocks(stockResponses)
                            .build();
                })
                .collect(Collectors.toList());

        return Optional.of(tradeHistoryResponses);
    }



}
