package com.jay.springbootmall.coupon.service.impl;

import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.dto.CreateCouponRequest;
import com.jay.springbootmall.coupon.model.Coupon;
import com.jay.springbootmall.coupon.model.UserCoupon;
import com.jay.springbootmall.coupon.repository.CouponJdbcRepository;
import com.jay.springbootmall.coupon.repository.CouponRepository;
import com.jay.springbootmall.coupon.repository.UserCouponRepository;
import com.jay.springbootmall.coupon.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponJdbcRepository couponJdbcRepository;

    public CouponServiceImpl(CouponRepository couponRepository,
                             UserCouponRepository userCouponRepository,
                             CouponJdbcRepository couponJdbcRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.couponJdbcRepository = couponJdbcRepository;
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("折扣碼已存在: " + request.getCode());
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("生效時間不可晚於過期時間");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setTitle(request.getTitle());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinSpending(request.getMinSpending());
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setTotalQuantity(request.getTotalQuantity());
        coupon.setUsedQuantity(0);

        Coupon savedCoupon = couponRepository.save(coupon);
        return convertToCouponResponse(savedCoupon, null);
    }

    @Override
    @Transactional
    public void issueCouponToMember(Long memberId, String couponCode) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("找不到對應的折扣碼"));

        // 1. 驗證有效期限
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new IllegalStateException("該優惠券不在有效期限內");
        }

        // 2. 檢查會員是否已經領過
        if (userCouponRepository.existsByMemberIdAndCoupon_CouponId(memberId, coupon.getCouponId())) {
            throw new IllegalStateException("該會員已領取過此優惠券");
        }

        // 3. 原子扣減發放名額（防止高併發超發）
        int updated = couponJdbcRepository.incrementUsedQuantity(coupon.getCouponId());
        if (updated == 0) {
            throw new IllegalStateException("該優惠券已被領取完畢");
        }

        // 4. 寫入領取紀錄
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setMemberId(memberId);
        userCoupon.setCoupon(coupon);
        userCoupon.setIsUsed(false);
        userCouponRepository.save(userCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getMemberCoupons(Long memberId) {
        return userCouponRepository.findAllByMemberIdWithCoupon(memberId).stream()
                .map(this::convertToCouponResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void useCoupon(Long memberId, Long couponId) {
        // 先查出該張券驗證效期（或直接在 SQL 限制）
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("找不到優惠券"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new IllegalStateException("該優惠券已過期或尚未生效");
        }

        // 原子更新使用狀態
        int updatedRows = couponJdbcRepository.markCouponAsUsed(memberId, couponId);
        if (updatedRows == 0) {
            throw new IllegalStateException("優惠券核銷失敗，可能已使用過或尚未領取");
        }
    }

    // 抽取共用的轉換方法（可傳入 isUsed 或 null）
    private CouponResponse convertToCouponResponse(Coupon coupon, Boolean isUsed) {
        CouponResponse dto = new CouponResponse();
        dto.setCouponId(coupon.getCouponId());
        dto.setCode(coupon.getCode());
        dto.setTitle(coupon.getTitle());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMinSpending(coupon.getMinSpending());
        dto.setStartTime(coupon.getStartTime());
        dto.setEndTime(coupon.getEndTime());
        dto.setIsUsed(isUsed);
        return dto;
    }

    private CouponResponse convertToCouponResponse(UserCoupon userCoupon) {
        return convertToCouponResponse(userCoupon.getCoupon(), userCoupon.getIsUsed());
    }
}