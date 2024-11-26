package back.whats_your_ETF.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "nickname", length = 30, nullable = false)
    private String nickname;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "level", nullable = false)
    private Long level;

    @Column(name = "image", length = 100)
    private String image;

    @Column(name = "member", nullable = false)
    private Boolean member;

    @Column(name = "asset")
    private Long asset;

    @Column(name = "subscriber_count")
    private Long subscriberCount;

    @Column(name = "is_in_top_10", nullable = false)
    private Boolean isInTop10;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolioss;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscribe> subscriptions;

    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscribe> subscribers;

}
