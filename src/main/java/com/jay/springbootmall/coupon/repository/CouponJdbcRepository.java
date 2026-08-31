package com.jay.springbootmall.coupon.repository;

public interface CouponJdbcRepository {
    int incrementUsedQuantity(Long couponId);
    int markCouponAsUsed(Long memberId, Long couponId);
}
