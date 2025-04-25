package cwchoiit.couponcore.repository.mysql;

import cwchoiit.couponcore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query(
            nativeQuery = true,
            value = "select * from coupon where coupon_id = :couponId for update"
    )
    Optional<Coupon> findLockedByCouponId(@Param("couponId") Long couponId);
}
