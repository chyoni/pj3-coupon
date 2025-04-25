package cwchoiit.couponapi.controller;

import cwchoiit.couponapi.controller.response.ApiResponse;
import cwchoiit.couponapi.service.CouponIssueRequestService;
import cwchoiit.couponapi.service.request.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons/api")
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/v1/issue")
    public ResponseEntity<ApiResponse<Void>> issueCoupon(@RequestBody CouponIssueRequest request) {
        couponIssueRequestService.requestIssue(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
