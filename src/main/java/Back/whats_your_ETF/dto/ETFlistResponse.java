package Back.whats_your_ETF.dto;

import lombok.*;

import java.util.List;

public record ETFlistResponse(List<PortfolioResponse> portfolios) {}
