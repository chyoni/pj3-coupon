package cwchoiit.couponcore.service;

import cwchoiit.couponcore.model.CouponIssued;
import cwchoiit.couponcore.repository.CouponIssuedQueryDslRepository;
import cwchoiit.couponcore.repository.CouponIssuedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.DUPLICATED_COUPON_ISSUED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponIssueService {

    private final CouponService couponService;
    private final CouponIssuedRepository couponIssuedRepository;
    private final CouponIssuedQueryDslRepository couponIssuedQueryDslRepository;

    @Transactional
    public void issue(Long couponId, Long userId) {
        couponService.findById(couponId).issue();
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

    private void checkAlreadyIssuance(Long couponId, Long userId) {
        Optional.ofNullable(couponIssuedQueryDslRepository.findCouponIssue(couponId, userId))
                .ifPresent(alreadyIssued -> {
                    throw DUPLICATED_COUPON_ISSUED.build(alreadyIssued.getCouponId(), alreadyIssued.getUserId());
                });
    }
}
