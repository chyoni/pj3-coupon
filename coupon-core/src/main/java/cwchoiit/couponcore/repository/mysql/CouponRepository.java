package cwchoiit.couponcore.repository.mysql;

import cwchoiit.couponcore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
