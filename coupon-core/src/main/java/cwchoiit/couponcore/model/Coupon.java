package cwchoiit.couponcore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.INVALID_COUPON_ISSUE_DATE;
import static cwchoiit.couponcore.exception.CouponCoreErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Getter
@Entity
@Builder
@Table(name = "coupon")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return issuedQuantity < totalQuantity;
    }

    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dateIssueStart) && now.isBefore(dateIssueEnd);
    }

    public void issue() {
        if (!availableIssueQuantity()) {
            throw INVALID_COUPON_ISSUE_QUANTITY.build(issuedQuantity, totalQuantity);
        }
        if (!availableIssueDate()) {
            throw INVALID_COUPON_ISSUE_DATE.build(LocalDateTime.now(), dateIssueStart, dateIssueEnd);
        }
        issuedQuantity++;
    }

    public boolean isIssueCompleted() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueEnd.isBefore(now) || !availableIssueQuantity();
    }
}
