package cwchoiit.couponcore.repository;

import cwchoiit.couponcore.model.CouponIssued;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssuedRepository extends JpaRepository<CouponIssued, Long> {
}
