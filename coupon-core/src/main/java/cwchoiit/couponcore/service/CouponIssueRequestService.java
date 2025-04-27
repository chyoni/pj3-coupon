package cwchoiit.couponcore.service;

import cwchoiit.couponcore.component.DistributeLockExecutor;
import cwchoiit.couponcore.exception.CouponCoreErrorCode;
import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.service.response.CouponReadResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cwchoiit.couponcore.exception.CouponCoreErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private static final String QUEUE_KEY = "issued:queue:couponId";

    private final RedisTemplate<String, String> redisTemplate;
    private final CouponService couponService;
    private final DistributeLockExecutor distributeLockExecutor;

    public void request(Long couponId, Long userId) {
        CouponReadResponse couponReadResponse = couponService.findCouponByLocalCache(couponId);
        Coupon coupon = Coupon.builder()
                .couponId(couponReadResponse.getCouponId())
                .title(couponReadResponse.getTitle())
                .couponType(couponReadResponse.getCouponType())
                .totalQuantity(couponReadResponse.getTotalQuantity())
                .issuedQuantity(couponReadResponse.getIssuedQuantity())
                .dateIssueStart(couponReadResponse.getDateIssueStart())
                .dateIssueEnd(couponReadResponse.getDateIssueEnd())
                .build();

        if (!coupon.availableIssueQuantity()) {
            throw INVALID_COUPON_ISSUE_QUANTITY.build(coupon.getIssuedQuantity(), coupon.getTotalQuantity());
        }

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
                                redisTemplate.opsForSet().size(generateKey(couponId)),
                                coupon.getTotalQuantity()
                        );
                    }

                    if (!availableUserIssueQuantity(couponId, userId)) {
                        throw DUPLICATED_COUPON_ISSUED.build(couponId, userId);
                    }

                    redisTemplate.opsForSet()
                            .add(generateKey(couponId), String.valueOf(userId));
                    redisTemplate.opsForList()
                            .rightPush(generateQueueKey(couponId), String.valueOf(userId));
                }
        );
    }

    public Long issuedQueueSize(Long couponId) {
        return redisTemplate.opsForList().size(generateQueueKey(couponId));
    }

    public Long findUserIdByIssuedCoupon(Long couponId) {
        String userId = redisTemplate.opsForList().leftPop(generateQueueKey(couponId));
        return userId == null ? null : Long.valueOf(userId);
    }

    public Boolean availableUserIssueQuantity(Long couponId, Long userId) {
        return Boolean.FALSE.equals(redisTemplate.opsForSet()
                .isMember(generateKey(couponId), String.valueOf(userId)));
    }

    public boolean availableTotalIssueQuantity(Long couponId, Integer totalQuantity) {
        if (totalQuantity == null) {
            return true;
        }
        return Objects.requireNonNull(redisTemplate.opsForSet().size(generateKey(couponId))).intValue() < totalQuantity;
    }

    private String generateKey(Long couponId) {
        return "issue:request:couponId:%d".formatted(couponId);
    }

    private String generateQueueKey(Long couponId) {
        return "%s:%d".formatted(QUEUE_KEY, couponId);
    }

    public void issueByScript(Long couponId, Long userId, Integer totalIssueQuantity) {
        String code = redisTemplate.execute(
                requestScript(),
                List.of(generateKey(couponId), generateQueueKey(couponId)),
                String.valueOf(userId),
                String.valueOf(totalIssueQuantity),
                String.valueOf(userId)
        );

        CouponIssueRequestScriptResult.check(CouponIssueRequestScriptResult.findCode(code));
    }

    private RedisScript<String> requestScript() {
        String script = """
                if redis.call('sismember', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                if redis.call('scard', KEYS[1]) < tonumber(ARGV[2]) then
                    redis.call('sadd', KEYS[1], ARGV[1])
                    redis.call('rpush', KEYS[2], ARGV[3])
                    return '1'
                end
                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }

    @AllArgsConstructor
    public enum CouponIssueRequestScriptResult {
        SUCCESS(1),
        DUPLICATED_COUPON_ISSUE(2),
        INVALID_COUPON_ISSUE_QUANTITY(3);

        private final int code;

        public static CouponIssueRequestScriptResult findCode(String code) {
            int codeValue = Integer.parseInt(code);
            for (CouponIssueRequestScriptResult result : values()) {
                if (result.code == codeValue) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Invalid CouponIssueScriptResult code: %s".formatted(code));
        }

        public static void check(CouponIssueRequestScriptResult result) {
            if (result == INVALID_COUPON_ISSUE_QUANTITY) {
                throw CouponCoreErrorCode.INVALID_COUPON_ISSUE_QUANTITY.build();
            }
            if (result == DUPLICATED_COUPON_ISSUE) {
                throw CouponCoreErrorCode.DUPLICATED_COUPON_ISSUED.build();
            }
        }
    }
}
