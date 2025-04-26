package cwchoiit.couponcore.service;

import cwchoiit.couponcore.component.DistributeLockExecutor;
import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.repository.redis.RedisRepository;
import cwchoiit.couponcore.service.response.CouponReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final RedisRepository redisRepository;
    private final CouponService couponService;
    private final DistributeLockExecutor distributeLockExecutor;

    public void issue(Long couponId, Long userId) {
        CouponReadResponse couponReadResponse = couponService.findCoupon(couponId);
        Coupon coupon = Coupon.builder()
                .couponId(couponReadResponse.getCouponId())
                .title(couponReadResponse.getTitle())
                .couponType(couponReadResponse.getCouponType())
                .totalQuantity(couponReadResponse.getTotalQuantity())
                .issuedQuantity(couponReadResponse.getIssuedQuantity())
                .dateIssueStart(couponReadResponse.getDateIssueStart())
                .dateIssueEnd(couponReadResponse.getDateIssueEnd())
                .build();

        if (!coupon.availableIssueDate()) {
            throw INVALID_COUPON_ISSUE_DATE.build(
                    LocalDateTime.now(),
                    coupon.getDateIssueStart(),
                    coupon.getDateIssueEnd()
            );
        }

        distributeLockExecutor.execute(
                "lock_%s".formatted(couponId),
                3000,
                3000,
                () -> {
                    if (!availableTotalIssueQuantity(couponId, coupon.getTotalQuantity())) {
                        throw INVALID_COUPON_ISSUE_QUANTITY.build(
                                redisRepository.sCard(generateKey(couponId)),
                                coupon.getTotalQuantity()
                        );
                    }

                    if (!availableUserIssueQuantity(couponId, userId)) {
                        throw DUPLICATED_COUPON_ISSUED.build(couponId, userId);
                    }

                    redisRepository.sAdd(generateKey(couponId), String.valueOf(userId));
                    redisRepository.rPush(generateQueueKey(couponId), String.valueOf(userId));
                }
        );
    }

    public boolean availableUserIssueQuantity(Long couponId, Long userId) {
        return !redisRepository.sIsMember(
                generateKey(couponId),
                String.valueOf(userId)
        );
    }

    public boolean availableTotalIssueQuantity(Long couponId, Integer totalQuantity) {
        if (totalQuantity == null) {
            return true;
        }
        return redisRepository.sCard(generateKey(couponId)) < totalQuantity;
    }

    private String generateKey(Long couponId) {
        return "issue:request:couponId:%d".formatted(couponId);
    }

    private String generateQueueKey(Long couponId) {
        return "issued:queue:couponId:%d".formatted(couponId);
    }
}
