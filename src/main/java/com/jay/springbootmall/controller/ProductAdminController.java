package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.ProductRequestDTO;
import com.jay.springbootmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
public class ProductAdminController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "建立新商品", description = "後台管理人員或初始化資料時使用")
    @PostMapping // 這裡會自動拼接成 /api/v1/products
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        Product savedProduct = productService.createProduct(productRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId,
                                                 @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        Product updatedProduct = productService.updateProduct(productId, productRequestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build(); // 回傳 204 No Content
    }

    @Operation(summary = "恢復已軟刪除的商品", description = "將商品的 is_deleted 狀態改回 0，使其重新出現在前台商城。")
    @PatchMapping("/{productId}/restore")
    public ResponseEntity<Void> restoreProduct(@PathVariable Integer productId) {
        productService.restoreProductById(productId);
        return ResponseEntity.ok().build(); // 回傳 200 OK
    }

    @Operation(summary = "後台：獲取所有商品列表（含上下架）", description = "供管理員後台使用，支援分頁與依據上/下架狀態過濾。")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsForAdmin(
            @Parameter(description = "過濾刪除狀態：0=未刪除(上架中), 1=已刪除(下架), 不傳=看全部")
            @RequestParam(required = false) Integer isDeleted,

            @Parameter(description = "頁碼 (從 0 開始)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "每頁筆數", example = "10")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<Product> products = productService.getAllProductsForAdmin(isDeleted, page, size);
        return ResponseEntity.ok(products);
    }
}