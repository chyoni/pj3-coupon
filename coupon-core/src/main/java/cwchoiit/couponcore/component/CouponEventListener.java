package cwchoiit.couponcore.component;

import cwchoiit.couponcore.model.CouponIssueCompleteEvent;
import cwchoiit.couponcore.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void issueCompleted(CouponIssueCompleteEvent event) {
        log.info("[issueCompleted] Coupon issue completed. cache refresh start coupon ID = {}", event.couponId());
        couponService.putCouponCache(event.couponId());
        couponService.putCouponLocalCache(event.couponId());
        log.info("[issueCompleted] Coupon issue completed. cache refresh end coupon ID = {}", event.couponId());
    }
}
