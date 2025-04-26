package cwchoiit.couponconsumer.component;

import cwchoiit.couponcore.service.CouponIssueRequestService;
import cwchoiit.couponcore.service.CouponIssueService;
import cwchoiit.couponcore.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueListener {

    private final CouponIssueRequestService couponIssueRequestService;
    private final CouponService couponService;
    private final CouponIssueService couponIssueService;

    @Scheduled(fixedDelay = 10000L, initialDelay = 10000L, timeUnit = TimeUnit.MILLISECONDS)
    public void issue() {
        log.debug("[issue] Schedule issue ...");
        couponService.findAll()
                .forEach(coupon -> {
                    while (existCouponIssued(coupon.getCouponId())) {
                        log.debug("[issue] Coupon {} is issuing", coupon.getCouponId());
                        Long userId = findUserIdByIssuedCoupon(coupon.getCouponId());
                        couponIssueService.issue(coupon.getCouponId(), userId);
                        log.debug("[issue] Coupon {} issued to user {}", coupon.getCouponId(), userId);
                    }
                });
    }

    private boolean existCouponIssued(Long couponId) {
        return couponIssueRequestService.issuedQueueSize(couponId) > 0;
    }

    private Long findUserIdByIssuedCoupon(Long couponId) {
        return couponIssueRequestService.findUserIdByIssuedCoupon(couponId);
    }
}
