package com.jay.springbootmall.coupon.model;

public enum DiscountType {
    FIXED_AMOUNT("折抵固定金額"),
    PERCENTAGE("打折/折扣比率");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
