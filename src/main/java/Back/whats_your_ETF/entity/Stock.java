package Back.whats_your_ETF.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long id;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "price", nullable = false)
    private Long price;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ETFStock> etfStocks;
}

