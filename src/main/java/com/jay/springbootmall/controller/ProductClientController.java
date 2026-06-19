package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "取得商品列表", description = "可帶入 category 參數篩選特定分類的商品（如心理測驗專用的 FRAGRANCE_PACK）")
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category) {

        List<Product> productList;

        if (category != null) {
            // 如果有傳分類，就走分類查詢
            productList = productService.getProductsByCategory(category);
        } else {
            // 如果沒傳，就撈出全部商品（原本的 JPA 內建功能）
            productList = productService.getAllProducts();
        }

        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

}
