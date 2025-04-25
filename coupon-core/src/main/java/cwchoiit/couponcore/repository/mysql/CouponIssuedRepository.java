package cwchoiit.couponcore.repository.mysql;

import cwchoiit.couponcore.model.CouponIssued;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssuedRepository extends JpaRepository<CouponIssued, Long> {
}
