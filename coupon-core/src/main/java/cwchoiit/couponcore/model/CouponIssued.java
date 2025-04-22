package cwchoiit.couponcore.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupon_issued")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponIssued extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long issueId;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime dateIssued;

    private LocalDateTime dateUsed;
}
