package cwchoiit.couponcore.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cwchoiit.couponcore.model.CouponIssued;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static cwchoiit.couponcore.model.QCouponIssued.couponIssued;

@Repository
@RequiredArgsConstructor
public class CouponIssuedQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CouponIssued findCouponIssue(Long couponId, Long userId) {
        return queryFactory.select(couponIssued)
                .from(couponIssued)
                .where(couponIdEquals(couponId), userIdEquals(userId))
                .fetchFirst();
    }

    private BooleanExpression userIdEquals(Long userId) {
        return couponIssued.userId.eq(userId);
    }

    private BooleanExpression couponIdEquals(Long couponId) {
        return couponIssued.couponId.eq(couponId);
    }
}
