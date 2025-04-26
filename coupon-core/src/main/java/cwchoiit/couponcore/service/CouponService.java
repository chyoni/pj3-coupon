package cwchoiit.couponcore.service;

import cwchoiit.couponcore.repository.mysql.CouponRepository;
import cwchoiit.couponcore.service.response.CouponReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.COUPON_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Cacheable(cacheNames = "coupons", key = "#couponId")
    public CouponReadResponse findCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .map(CouponReadResponse::of)
                .orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }
}
