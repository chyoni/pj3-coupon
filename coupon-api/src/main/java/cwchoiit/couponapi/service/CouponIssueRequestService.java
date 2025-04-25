package cwchoiit.couponapi.service;

import cwchoiit.couponapi.service.request.CouponIssueRequest;
import cwchoiit.couponcore.component.DistributeLockExecutor;
import cwchoiit.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    public void requestIssue(CouponIssueRequest request) {
        distributeLockExecutor.execute(
                "lock_%s".formatted(request.couponId()),
                10000,
                10000,
                () -> couponIssueService.issue(request.couponId(), request.userId())
        );
        log.info("[requestIssue] Coupon {} issued to user {}", request.couponId(), request.userId());
    }
}
