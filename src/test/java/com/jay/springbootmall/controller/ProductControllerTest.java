package com.jay.springbootmall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.springbootmall.dao.ProductRepository;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.ProductCategory;
import com.jay.springbootmall.model.ProductRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 💡 超重要：測試完自動 Rollback（回滾），不會弄髒資料庫！
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper; // 用來把 DTO 轉成 JSON 字串

    // ==========================================
    // 1. 測試：修改功能 (PUT) 當商品下架時要擋下來
    // ==========================================
    @Test
    @DisplayName("後台修改商品 - 當商品已下架時，應回傳 400 錯誤與對應提示")
    void updateProduct_whenDeleted_shouldReturn400() throws Exception {
        // Arrange: 先塞一筆「已下架 (is_deleted = 1)」的商品到資料庫
        Product deletedProduct = new Product();
        deletedProduct.setProductName("舊除濕機");
        deletedProduct.setBrand("Muji");
        deletedProduct.setCategory(ProductCategory.DEHUMIDIFIER); // 確保用 Enum
        deletedProduct.setPrice(1000);
        deletedProduct.setStock(10);
        deletedProduct.setImageUrl("https://example.com/img.png");
        deletedProduct.setIsDeleted(1); // 設為下架
        deletedProduct = productRepository.saveAndFlush(deletedProduct);

        // 準備修改的 DTO 資料
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setProductName("新除濕機名稱");
        requestDTO.setBrand("Muji");
        requestDTO.setPrice(1200);
        requestDTO.setStock(5);
        requestDTO.setImageUrl("https://example.com/img.png");
        requestDTO.setProductSpec(new HashMap<>());

        // 🎯 關鍵修正：幫 DTO 補上那些被 @NotNull 限制的必填欄位
        requestDTO.setCategory(ProductCategory.DEHUMIDIFIER);
        requestDTO.setIsPromo(false); // 或是你的布林、整數型態，確保它不是 null

        // Act & Assert: 模擬發送 PUT 請求
        mockMvc.perform(put("/api/v1/admin/products/" + deletedProduct.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                // 預期結果：這次能順利越過參數驗證防線，挺進 Service 層觸發你的「狀態卡控」丟出 RuntimeException
                .andExpect(status().isBadRequest())
                // 預期全域異常處理器攔截 RuntimeException 輸出的格式
                .andExpect(jsonPath("$.message", containsString("該商品目前處於下架/刪除狀態")));
    }

    // ==========================================
    // 2. 測試：下架功能 (DELETE) 重複下架要擋下來
    // ==========================================
    @Test
    @DisplayName("後台下架商品 - 當商品早已下架時，重複下架應回傳 400")
    void deleteProduct_whenAlreadyDeleted_shouldReturn400() throws Exception {
        // Arrange: 塞一筆早已下架的商品
        Product deletedProduct = new Product();
        deletedProduct.setProductName("絕版防潮袋");
        deletedProduct.setBrand("山水");

        // 🎯 【關鍵修正】這裡一定要補上 category，不然資料庫會噴 Column 'category' cannot be null
        deletedProduct.setCategory(ProductCategory.FRAGRANCE_PACK);

        deletedProduct.setPrice(500);
        deletedProduct.setStock(0);
        deletedProduct.setImageUrl("https://example.com/img2.png");
        deletedProduct.setIsDeleted(1); // 早已下架

        // 💡 如果你的 Product Entity 還有其他 @Column(nullable = false) 的欄位（例如 isPromo），記得在這裡一併塞預設值：
        // deletedProduct.setIsPromo(false);

        deletedProduct = productRepository.saveAndFlush(deletedProduct);

        // Act & Assert: 模擬發送 DELETE 請求
        mockMvc.perform(delete("/api/v1/admin/products/" + deletedProduct.getProductId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("該商品目前已處於下架狀態")));
    }
}