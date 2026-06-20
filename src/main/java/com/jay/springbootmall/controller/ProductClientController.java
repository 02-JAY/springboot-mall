package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品相關 API", description = "提供商品查詢、新增等電商核心功能")
@RestController
@RequestMapping("/api/v1/products") // 1. 統一管理基本路徑與版本
public class ProductClientController {
    @Autowired
    private ProductService productService;

    @Operation(summary = "取得單一商品詳情", description = "根據商品 ID 查詢商品詳細資訊，若找不到則回傳 404")
    @GetMapping("/{productId}") // 這裡會自動拼接成 /api/v1/products/{productId}
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId) {
        Product product = productService.getProductById(productId);


        return ResponseEntity.status(HttpStatus.OK).body(product);

    }

    @Operation(summary = "多功能條件搜尋商品", description = "支援關鍵字模糊查詢、分類篩選、價格區間篩選。不傳的條件代表不限制。")
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @Parameter(description = "關鍵字模糊查詢（商品名稱或品牌）", example = "消臭包")
            @RequestParam(required = false) String search,

            @Parameter(description = "分類篩選", example = "FRAGRANCE_PACK")
            @RequestParam(required = false) String category,

            @Parameter(description = "最低價格", example = "50")
            @RequestParam(required = false) Integer minPrice,

            @Parameter(description = "最高價格", example = "300")
            @RequestParam(required = false) Integer maxPrice,

            @Parameter(description = "是否只篩選特價/優惠商品", example = "true")
            @RequestParam(required = false) Boolean isPromotion,

            // 新增：JDBC 分頁與排序參數（設定預設值）
            @Parameter(description = "分頁頁碼（從 0 開始）", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "每頁筆數", example = "10")
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "排序欄位", example = "created_date")
            @RequestParam(defaultValue = "created_date") String orderBy,

            @Parameter(description = "排序方向", example = "desc")
            @RequestParam(defaultValue = "desc") String sort
    ) {
        // Service 層除了處理搜尋，還要負責：
        // 1. 自動加上 WHERE deleted_at IS NULL (軟刪除過濾，前台不顯示已刪除商品)
        // 2. 處理分頁 (LIMIT = size, OFFSET = page * size)
        List<Product> products = productService.getProducts(
                search, category, minPrice, maxPrice, isPromotion, page, size, orderBy, sort
        );

        return ResponseEntity.ok(products);
    }

}
