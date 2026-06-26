package com.jay.springbootmall.model;

public enum ProductCategory {
    FRAGRANCE_PACK("香氛防潮包"),
    DEHUMIDIFIER("除濕機"),
    DRY_BOX("電子防潮箱");

    private final String displayName;

    // 建構子
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    // 讓外部可以取得中文字義
    public String getDisplayName() {
        return displayName;
    }
}