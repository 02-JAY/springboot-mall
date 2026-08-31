package com.jay.springbootmall.coupon.repository;

import com.jay.springbootmall.coupon.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    boolean existsByMemberIdAndCoupon_CouponId(Long memberId, Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.memberId = :memberId")
    List<UserCoupon> findAllByMemberIdWithCoupon(@Param("memberId") Long memberId);
}
