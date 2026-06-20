package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.UpdateProductDTO;
import com.jay.springbootmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
public class ProductAdminController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "建立新商品", description = "後台管理人員或初始化資料時使用")
    @PostMapping // 這裡會自動拼接成 /api/v1/products
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId,
                                                 @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        Product updatedProduct = productService.updateProduct(productId, updateProductDTO);
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
}