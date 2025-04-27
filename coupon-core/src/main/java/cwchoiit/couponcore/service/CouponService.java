package cwchoiit.couponcore.service;

import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.repository.CouponRepository;
import cwchoiit.couponcore.service.response.CouponReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.COUPON_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    @Cacheable(cacheNames = "coupons", key = "#couponId")
    public CouponReadResponse findCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .map(CouponReadResponse::of)
                .orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }

    @Cacheable(cacheNames = "coupons", cacheManager = "localCacheManager")
    public CouponReadResponse findCouponByLocalCache(Long couponId) {
        return proxy().findCoupon(couponId);
    }

    @CachePut(cacheNames = "coupons")
    public CouponReadResponse putCouponCache(Long couponId) {
        return couponRepository.findById(couponId)
                .map(CouponReadResponse::of)
                .orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }

    @CachePut(cacheNames = "coupons", cacheManager = "localCacheManager")
    public CouponReadResponse putCouponLocalCache(Long couponId) {
        return couponRepository.findById(couponId)
                .map(CouponReadResponse::of)
                .orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public Coupon findById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }

    private CouponService proxy() {
        return (CouponService) AopContext.currentProxy();
    }
}
