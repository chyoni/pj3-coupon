package cwchoiit.couponapi.service;

import cwchoiit.couponapi.service.request.CouponIssueRequest;
import cwchoiit.couponcore.component.DistributeLockExecutor;
import cwchoiit.couponcore.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponApiIssueRequestService {
    private final CouponIssueRequestService couponIssueRequestService;
    private final DistributeLockExecutor distributeLockExecutor;

    public void requestIssue(CouponIssueRequest request) {
        distributeLockExecutor.execute(
                "lock_%s".formatted(request.couponId()),
                10000,
                10000,
                () -> couponIssueRequestService.request(request.couponId(), request.userId())
        );

        log.info("[requestIssue] Coupon {} issued to user {}", request.couponId(), request.userId());
    }
}
