package back.whats_your_ETF.entity;

import back.whats_your_ETF.global.BasicEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "revenue")
    private Double revenue;

    @Column(name = "invest_amount")
    private Long investAmount;

    @Column(name = "is_etf", nullable = false)
    private Boolean isEtf;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ETFStock> etfStocks;

    @Column(name = "profit_spot")
    private Long profitSpot = 100L;

    @Column(name = "loss_spot")
    private Long lossSpot = -100L;

    @Column
    private boolean alreadySend = false;

}
