package Back.whats_your_ETF.service;

import Back.whats_your_ETF.apiPayload.GeneralException;
import Back.whats_your_ETF.apiPayload.code.status.ErrorStatus;
import Back.whats_your_ETF.dto.EtfRequest;
import Back.whats_your_ETF.entity.ETFStock;
import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.entity.Stock;
import Back.whats_your_ETF.entity.User;
import Back.whats_your_ETF.repository.ETFStockRepository;
import Back.whats_your_ETF.repository.PortfolioRepository;
import Back.whats_your_ETF.repository.StockRepository;
import Back.whats_your_ETF.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EtfService {

    private final ETFStockRepository etfStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public void buyETF(Long userId, EtfRequest.etfInvestList etfInvestList) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Portfolio portfolio = portfolioRepository.findByUserAndIsEtfTrue(user)
                .orElseGet(() -> portfolioRepository.save(Portfolio.builder()
                        .user(user)
                        .title("ETF Portfolio")
                        .isEtf(true)
                        .build()));

        for (EtfRequest.etfInvest etfInvest : etfInvestList.getEtfList()) {
            Stock stock = stockRepository.findByStockCode(etfInvest.getStockCode())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

            ETFStock etfStock = ETFStock.builder()
                    .portfolio(portfolio)
                    .stock(stock)
                    .percentage(etfInvest.getPercentage())
                    .purchasePrice(etfInvest.getPrice())
                    .build();

            etfStockRepository.save(etfStock);
        }
    }
}
