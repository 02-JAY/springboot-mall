package com.jay.springbootmall.coupon.controller;

import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.dto.CreateCouponRequest;
import com.jay.springbootmall.coupon.model.Coupon;
import com.jay.springbootmall.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "優惠券管理後台 API", description = "提供後台管理人員新增、設定與維護商城優惠券規則")
@RestController
@RequestMapping("/api/v1/admin/coupons") // 統一管理員路徑隔離
public class CouponAdminController {

    private final CouponService couponService;

    // 遵循現代化建構子注入，不使用 @Autowired 變數注入
    public CouponAdminController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "建立新優惠券", description = "管理員設定優惠券代碼、折扣類型 (金額/百分比)、最低消費門檻、生效與過期時間以及發放總數量上限。")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}