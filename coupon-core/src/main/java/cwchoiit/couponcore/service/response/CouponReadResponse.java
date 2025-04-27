package cwchoiit.couponcore.service.response;

import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.model.CouponType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CouponReadResponse {
    private Long couponId;
    private String title;
    private CouponType couponType;
    private Integer totalQuantity;
    private int issuedQuantity;
    private boolean availableIssueQuantity;
    private LocalDateTime dateIssueStart;
    private LocalDateTime dateIssueEnd;

    public static CouponReadResponse of(Coupon coupon) {
        CouponReadResponse response = new CouponReadResponse();
        response.couponId = coupon.getCouponId();
        response.title = coupon.getTitle();
        response.couponType = coupon.getCouponType();
        response.totalQuantity = coupon.getTotalQuantity();
        response.issuedQuantity = coupon.getIssuedQuantity();
        response.availableIssueQuantity = coupon.availableIssueQuantity();
        response.dateIssueStart = coupon.getDateIssueStart();
        response.dateIssueEnd = coupon.getDateIssueEnd();
        return response;
    }
}
