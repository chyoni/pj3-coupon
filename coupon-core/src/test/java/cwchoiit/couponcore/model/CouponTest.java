package cwchoiit.couponcore.model;

import cwchoiit.couponcore.exception.CouponCoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Model - Coupon")
class CouponTest {

    @Test
    @DisplayName("총 쿠폰 수량이 현재 발급된 쿠폰보다 많은 경우 쿠폰 발급 가능 상태이다.")
    void availableIssueQuantity_success() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(10)
                .build();

        assertThat(coupon.availableIssueQuantity()).isTrue();
    }

    @Test
    @DisplayName("발급 수량이 소진되었다면 쿠폰 발급 불가능 상태이다.")
    void availableIssueQuantity_fail() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        assertThat(coupon.availableIssueQuantity()).isFalse();
    }

    @Test
    @DisplayName("총 발급 수량에 제한이 없다면 언제든 쿠폰 발급 가능 상태이다.")
    void availableIssueQuantity_success_no_limit() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();

        assertThat(coupon.availableIssueQuantity()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 기한에 도달하지 않은 경우 쿠폰 발급은 불가능하다.")
    void availableIssueDate_fail() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        assertThat(coupon.availableIssueDate()).isFalse();
    }

    @Test
    @DisplayName("쿠폰 발급 기한인 경우, 쿠폰 발급이 가능하다.")
    void availableIssueDate_success() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        assertThat(coupon.availableIssueDate()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 기한이 종료되면, 쿠폰 발급이 불가능하다.")
    void availableIssueDate_fail_expired() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        assertThat(coupon.availableIssueDate()).isFalse();
    }

    @Test
    @DisplayName("쿠폰 발급 기한에 해당하고, 쿠폰 발급 가능 개수가 소진되지 않았다면 쿠폰 발급이 가능하다.")
    void availableIssueDate_and_availableIssueQuantity_success() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .issuedQuantity(10)
                .totalQuantity(100)
                .build();

        assertThat(coupon.availableIssueDate()).isTrue();
        assertThat(coupon.availableIssueQuantity()).isTrue();

        coupon.issue();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(11);
    }

    @Test
    @DisplayName("쿠폰 발급 기한에 해당하지 않으면, 쿠폰 발급 가능 개수가 소진되지 않았더라도 쿠폰 발급이 불가능하다.")
    void availableIssueDate_and_availableIssueQuantity_fail() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .issuedQuantity(10)
                .totalQuantity(100)
                .build();

        assertThat(coupon.availableIssueDate()).isFalse();
        assertThat(coupon.availableIssueQuantity()).isTrue();

        assertThatThrownBy(coupon::issue).isInstanceOf(CouponCoreException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0002");
    }

    @Test
    @DisplayName("쿠폰 발급 기한에 해당하지만, 쿠폰 발급 가능 개수가 소진되었다면 쿠폰 발급이 불가능하다.")
    void availableIssueDate_and_availableIssueQuantity_fail_2() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .issuedQuantity(100)
                .totalQuantity(100)
                .build();

        assertThat(coupon.availableIssueDate()).isTrue();
        assertThat(coupon.availableIssueQuantity()).isFalse();

        assertThatThrownBy(coupon::issue).isInstanceOf(RuntimeException.class)
                .hasFieldOrPropertyWithValue("code", "COUPON-C-0001");
    }
}