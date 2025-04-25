package cwchoiit.couponcore.service;

import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.model.CouponIssued;
import cwchoiit.couponcore.repository.mysql.CouponIssuedQueryDslRepository;
import cwchoiit.couponcore.repository.mysql.CouponIssuedRepository;
import cwchoiit.couponcore.repository.mysql.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.COUPON_NOT_FOUND;
import static cwchoiit.couponcore.exception.CouponCoreErrorCode.DUPLICATED_COUPON_ISSUED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final CouponIssuedRepository couponIssuedRepository;
    private final CouponIssuedQueryDslRepository couponIssuedQueryDslRepository;

    @Transactional
    public void issue(Long couponId, Long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();

        saveCouponIssued(couponId, userId);
    }

    @Transactional
    public CouponIssued saveCouponIssued(Long couponId, Long userId) {
        checkAlreadyIssuance(couponId, userId);
        return couponIssuedRepository.save(
                CouponIssued.of(
                        couponId,
                        userId
                )
        );
    }

    public Coupon findCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> COUPON_NOT_FOUND.build(couponId));
    }

    private void checkAlreadyIssuance(Long couponId, Long userId) {
        Optional.ofNullable(couponIssuedQueryDslRepository.findCouponIssue(couponId, userId))
                .ifPresent(alreadyIssued -> {
                    throw DUPLICATED_COUPON_ISSUED.build(alreadyIssued.getCouponId(), alreadyIssued.getUserId());
                });
    }
}
