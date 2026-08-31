package com.jay.springbootmall.coupon.dto;

import com.jay.springbootmall.coupon.model.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "建立優惠券請求")
public class CreateCouponRequest {

    @NotBlank
    @Schema(description = "優惠券代碼", example = "LINE_QUIZ_2026")
    private String code;

    @NotBlank
    @Schema(description = "優惠券標題", example = "心理測驗限定折扣券")
    private String title;

    @NotNull
    @Schema(description = "折扣類型 (FIXED_AMOUNT / PERCENTAGE)", example = "FIXED_AMOUNT")
    private DiscountType discountType;

    @NotNull
    @Min(0)
    @Schema(description = "折扣數值", example = "100.00")
    private BigDecimal discountValue;

    @Schema(description = "最低消費門檻", example = "500.00")
    private BigDecimal minSpending;

    @NotNull
    @Schema(description = "生效時間")
    private LocalDateTime startTime;

    @NotNull
    @Future
    @Schema(description = "過期時間")
    private LocalDateTime endTime;

    @NotNull
    @Min(1)
    @Schema(description = "總數量上限", example = "1000")
    private Integer totalQuantity;

    public CreateCouponRequest() {
    }

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinSpending() {
        return minSpending;
    }

    public void setMinSpending(BigDecimal minSpending) {
        this.minSpending = minSpending;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}