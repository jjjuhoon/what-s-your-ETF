package Back.whats_your_ETF.service;

import Back.whats_your_ETF.dto.ETFStockResponse;
import Back.whats_your_ETF.dto.PortfolioDetailsResponse;
import Back.whats_your_ETF.entity.ETFStock;
import Back.whats_your_ETF.entity.Portfolio;
import Back.whats_your_ETF.repository.ETFStockRepository;
import Back.whats_your_ETF.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ETFStockRepository etfStockRepository;

    public Optional<PortfolioDetailsResponse> getPortfolioDetailsByPortfolioId(Long portfolioId) {

        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(portfolioId);

        if (portfolioOptional.isEmpty()) {
            return Optional.empty();
        }

        Portfolio portfolio = portfolioOptional.get();

        List<ETFStock> etfStocks = etfStockRepository.findAllByPortfolioId(portfolioId);

        List<ETFStockResponse> etfStockResponses = etfStocks.stream()
                .map(etfStock -> new ETFStockResponse(
                        etfStock.getStock().getStockCode(),
                        etfStock.getStock().getStockName(),
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
}
