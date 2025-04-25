package cwchoiit.couponcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponCoreErrorCode {
    INVALID_COUPON_ISSUE_QUANTITY("COUPON-C-0001", "Invalid coupon issue quantity. Current issued quantity is %s. Maximum issued quantity is %s."),
    INVALID_COUPON_ISSUE_DATE("COUPON-C-0002", "Invalid coupon issue date. Current date is %s. Date issue start is %s. Date issue end is %s."),
    DUPLICATED_COUPON_ISSUED("COUPON-C-0003", "Already issued coupon with [COUPON_ID] = %s [USER_ID] = %s"),
    COUPON_NOT_FOUND("COUPON-C-0004", "Coupon not found with ID = %s"),
    ;

    private final String code;
    private final String reason;

    public CouponCoreException build() {
        return new CouponCoreException(code, reason);
    }

    public CouponCoreException build(Object... args) {
        return new CouponCoreException(code, reason.formatted(args));
    }
}
