package cwchoiit.couponcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponCoreException extends RuntimeException {
  private final String code;
  private final String reason;
}
