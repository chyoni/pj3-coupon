package cwchoiit.couponcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponCoreErrorCode {
    INVALID_COUPON_ISSUE_QUANTITY("COUPON-C-0001", "Invalid coupon issue quantity. Current issued quantity is %s. Maximum issued quantity is %s."),
    INVALID_COUPON_ISSUE_DATE("COUPON-C-0002", "Invalid coupon issue date. Current date is %s. Date issue start is %s. Date issue end is %s."),
    INVALID_COUPON_DUPLICATED("COUPON-C-0003", "Duplicated coupon title."),
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
