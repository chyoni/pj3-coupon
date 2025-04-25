package cwchoiit.couponapi.service;

import cwchoiit.couponapi.service.request.CouponIssueRequest;
import cwchoiit.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;

    public void requestIssue(CouponIssueRequest request) {
        couponIssueService.issue(request.couponId(), request.userId());
        log.info("[requestIssue] Coupon {} issued to user {}", request.couponId(), request.userId());
    }
}
