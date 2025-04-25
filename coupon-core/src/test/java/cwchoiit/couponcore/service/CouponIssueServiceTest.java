package cwchoiit.couponcore.service;

import cwchoiit.couponcore.TestConfigurations;
import cwchoiit.couponcore.exception.CouponCoreException;
import cwchoiit.couponcore.model.Coupon;
import cwchoiit.couponcore.model.CouponIssued;
import cwchoiit.couponcore.model.CouponType;
import cwchoiit.couponcore.repository.mysql.CouponIssuedQueryDslRepository;
import cwchoiit.couponcore.repository.mysql.CouponIssuedRepository;
import cwchoiit.couponcore.repository.mysql.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Service - CouponIssueService")
class CouponIssueServiceTest extends TestConfigurations {

    @Autowired
    CouponIssueService couponIssueService;

    @Autowired
    CouponIssuedRepository couponIssuedRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponIssuedQueryDslRepository couponIssuedQueryDslRepository;

    @Test
    @DisplayName("기존에 쿠폰을 발급받았다면, 추가적으로 쿠폰을 발급하지 못한다.")
    void coupon_issued_fail() {
        CouponIssued couponIssued = CouponIssued.of(1L, 1L);
        couponIssuedRepository.save(couponIssued);

        assertThatThrownBy(() -> couponIssueService.saveCouponIssued(1L, 1L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0003");
    }

    @Test
    @DisplayName("기존에 쿠폰을 발급받지 않았다면, 쿠폰을 발급받을 수 있다.")
    void coupon_issued_success() {
        CouponIssued couponIssued = couponIssueService.saveCouponIssued(1L, 1L);
        assertThat(couponIssuedRepository.findById(couponIssued.getIssueId())).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 발급 성공 케이스")
    void issue_success() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build()
        );

        couponIssueService.issue(coupon.getCouponId(), 1L);

        Coupon savedCoupon = couponRepository.findById(coupon.getCouponId()).get();
        assertThat(savedCoupon).isNotNull();
        assertThat(savedCoupon.getIssuedQuantity()).isEqualTo(1);

        CouponIssued couponIssued = couponIssuedQueryDslRepository.findCouponIssue(coupon.getCouponId(), 1L);
        assertThat(couponIssued).isNotNull();
        assertThat(couponIssued.getUserId()).isEqualTo(1L);
        assertThat(couponIssued.getDateUsed()).isNull();
    }

    @Test
    @DisplayName("쿠폰 발급 실패 케이스 - 발급 수량 문제")
    void issue_fail_quantity() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build()
        );

        assertThatThrownBy(() -> couponIssueService.issue(coupon.getCouponId(), 1L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0001");
    }

    @Test
    @DisplayName("쿠폰 발급 실패 케이스 - 발급 기한 문제")
    void issue_fail_expired() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build()
        );

        assertThatThrownBy(() -> couponIssueService.issue(coupon.getCouponId(), 1L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0002");
    }

    @Test
    @DisplayName("쿠폰 발급 실패 케이스 - 중복 발급 문제")
    void issue_fail_duplicated() {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build()
        );

        couponIssueService.issue(coupon.getCouponId(), 1L);

        CouponIssued issuedCoupon = couponIssuedQueryDslRepository.findCouponIssue(coupon.getCouponId(), 1L);
        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getUserId()).isEqualTo(1L);
        assertThat(issuedCoupon.getCouponId()).isEqualTo(coupon.getCouponId());

        assertThatThrownBy(() -> couponIssueService.issue(coupon.getCouponId(), 1L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0003");
    }

    @Test
    @DisplayName("없는 쿠폰을 발급하려고 하면 예외 발생")
    void coupon_not_found() {
        assertThatThrownBy(() -> couponIssueService.issue(1L, 1L))
                .isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0004");
    }
}