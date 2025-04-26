package cwchoiit.couponcore.service;

import cwchoiit.couponcore.EmbeddedRedis;
import cwchoiit.couponcore.TestConfigurations;
import cwchoiit.couponcore.exception.CouponCoreException;
import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.model.CouponType;
import cwchoiit.couponcore.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(EmbeddedRedis.class)
@DisplayName("Service - CouponIssueRequestService")
class CouponIssueRequestServiceTest extends TestConfigurations {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CouponIssueRequestService couponIssueRequestService;

    @Autowired
    CouponRepository couponRepository;

    @BeforeEach
    public void beforeEach() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    @Test
    @DisplayName("쿠폰 발급 요청 - 쿠폰이 존재하지 않는다면 예외가 발생해야 한다.")
    void request_coupon_not_found() {
        assertThatThrownBy(() -> couponIssueRequestService.request(1L, 2L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0004");
    }

    @Test
    @DisplayName("쿠폰 발급 요청 기한 검증 - 발급 기한에 해당하지 않으면 예외가 발생해야 한다.")
    void available_request_date_fail() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build()
        );

        assertThatThrownBy(() -> couponIssueRequestService.request(coupon.getCouponId(), 2L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0002");
    }

    @Test
    @DisplayName("쿠폰 발급 요청 후 검증 - 쿠폰 발급이 정상적으로 처리됐다면, 해당 유저가 선착순 큐에 저장되어야 한다.")
    void issued_user_check() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build()
        );

        couponIssueRequestService.request(coupon.getCouponId(), 2L);

        // 중복 유저는 발급 받을 수 없어야 한다.
        boolean isOk = couponIssueRequestService.availableUserIssueQuantity(coupon.getCouponId(), 2L);
        assertThat(isOk).isFalse();

        Boolean isMember = redisTemplate.opsForSet()
                .isMember("issue:request:couponId:%s".formatted(coupon.getCouponId()), "2");
        assertThat(isMember).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 요청 후 검증 - 쿠폰 발급이 정상적으로 처리됐다면, 해당 유저가 발급 큐에 저장되어야 한다.")
    void issued_user_check_2() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build()
        );

        couponIssueRequestService.request(coupon.getCouponId(), 2L);

        // 중복 유저는 발급 받을 수 없어야 한다.
        boolean isOk = couponIssueRequestService.availableUserIssueQuantity(coupon.getCouponId(), 2L);
        assertThat(isOk).isFalse();

        String userId = redisTemplate.opsForList()
                .leftPop("issued:queue:couponId:%s".formatted(coupon.getCouponId()));
        assertThat(userId).isEqualTo("2");
    }

    @Test
    @DisplayName("쿠폰 요청 수량 검증 - 발급 가능 수량이 존재하면 쿠폰을 발급받을 수 있다.")
    void available_request_quantity_success() {
        boolean isOk = couponIssueRequestService.availableTotalIssueQuantity(1L, 100);
        assertThat(isOk).isTrue();
    }

    @Test
    @DisplayName("쿠폰 요청 수량 검증 - 발급 가능 수량이 없으면 쿠폰을 발급받을 수 없다.")
    void available_request_quantity_fail() {
        int totalQuantity = 100;

        Coupon coupon = couponRepository.save(Coupon.builder()
                        .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                        .title("선착순 테스트 쿠폰")
                        .totalQuantity(totalQuantity)
                        .issuedQuantity(0)
                        .dateIssueStart(LocalDateTime.now().minusDays(2))
                        .dateIssueEnd(LocalDateTime.now().plusDays(2))
                        .build()
        );

        IntStream.range(0, totalQuantity)
                .forEach(userId -> couponIssueRequestService.request(coupon.getCouponId(), (long) userId));

        boolean isOk = couponIssueRequestService.availableTotalIssueQuantity(coupon.getCouponId(), totalQuantity);
        assertThat(isOk).isFalse();

        assertThatThrownBy(() -> couponIssueRequestService.request(coupon.getCouponId(), 200L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0001");
    }

    @Test
    @DisplayName("쿠폰 중복 발급 요청 검증 - 발급된 내역에 유저가 존재하지 않으면 발급받을 수 있다.")
    void available_user_request_quantity_duplicated_success() {
        boolean isOk = couponIssueRequestService.availableUserIssueQuantity(2L, 2L);
        assertThat(isOk).isTrue();
    }

    @Test
    @DisplayName("쿠폰 중복 발급 요청 검증 - 발급된 내역에 유저가 존재하면 발급받을 수 없다.")
    void available_user_request_quantity_duplicated_fail() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build()
        );

        couponIssueRequestService.request(coupon.getCouponId(), 2L);

        boolean isOk = couponIssueRequestService.availableUserIssueQuantity(coupon.getCouponId(), 2L);
        assertThat(isOk).isFalse();

        assertThatThrownBy(() -> couponIssueRequestService.request(coupon.getCouponId(), 2L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0003");
    }
}