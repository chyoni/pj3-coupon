package cwchoiit.couponcore.exception;

import lombok.Getter;

@Getter
public class CouponCoreException extends RuntimeException {
    private final String code;
    private final String reason;

    public CouponCoreException(String code, String reason) {
        super(reason);
        this.code = code;
        this.reason = reason;
    }
}
