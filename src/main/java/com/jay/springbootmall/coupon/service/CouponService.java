package com.jay.springbootmall.coupon.service;

import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.dto.CreateCouponRequest;
import com.jay.springbootmall.coupon.model.Coupon;

import java.util.List;

public interface CouponService {
    CouponResponse createCoupon(CreateCouponRequest request);

    void issueCouponToMember(Long memberId, String couponCode);

    List<CouponResponse> getMemberCoupons(Long memberId);

    void useCoupon(Long memberId, Long couponId);
}
