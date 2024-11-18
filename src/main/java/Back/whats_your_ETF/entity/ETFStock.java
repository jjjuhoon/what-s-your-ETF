package Back.whats_your_ETF.entity;

import Back.whats_your_ETF.global.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "etf_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ETFStock extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_stock_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "purchase_price")
    private Long purchasePrice;
}

