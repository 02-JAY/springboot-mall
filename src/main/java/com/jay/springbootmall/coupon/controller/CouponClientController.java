package com.jay.springbootmall.coupon.controller;

import com.jay.springbootmall.coupon.dto.CouponResponse;
import com.jay.springbootmall.coupon.model.Coupon;
import com.jay.springbootmall.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "優惠券前台 API", description = "提供一般會員查詢個人優惠券、領取心理測驗限定折扣碼等功能")
@RestController
@RequestMapping("/api/v1/coupons") // 統一 v1 版本管理
public class CouponClientController {

    private final CouponService couponService;

    // 遵循現代化建構子注入，不使用 @Autowired 變數注入
    public CouponClientController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "發放優惠券給指定會員", description = "當會員在 LINE 官方帳號完成心理測驗或觸發行銷活動時，傳入會員 ID 與折扣碼字串進行綁定發放。")
    @PostMapping("/issue")
    public ResponseEntity<Void> issueCouponToMember(
            @RequestParam Long memberId,
            @RequestParam String couponCode) {
        couponService.issueCouponToMember(memberId, couponCode);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "查詢會員擁有的優惠券清單", description = "根據會員主鍵 ID 查詢該會員目前所擁有的所有優惠券資訊與可使用狀態。")
    @GetMapping("/my-coupons")
    public ResponseEntity<List<CouponResponse>> getMemberCoupons(@RequestParam Long memberId) {
        List<CouponResponse> response = couponService.getMemberCoupons(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "核銷/使用優惠券", description = "結帳時扣抵特定優惠券。將進行併發控制與狀態更新，標記該優惠券為已使用。")
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(
            @RequestParam Long memberId,
            @RequestParam Long couponId) {
        couponService.useCoupon(memberId, couponId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}