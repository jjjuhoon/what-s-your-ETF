package Back.whats_your_ETF.service;

import Back.whats_your_ETF.dto.*;
import Back.whats_your_ETF.entity.ETFStock;
import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.entity.User;
import Back.whats_your_ETF.repository.ETFStockRepository;
import Back.whats_your_ETF.repository.PortfolioRepository;
import Back.whats_your_ETF.repository.UserRepository;
import jakarta.transaction.Transactional;
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
                                    etfStock.getStock().getStockCode(),
                                    etfStock.getStock().getStockName(),
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
