package com.jay.springbootmall;

import com.jay.springbootmall.dao.ProductRepository;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.ProductCategory;
import com.jay.springbootmall.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class ProductServiceTest {
    @Autowired
    private ProductService productService; // 測試主角：注入你的 Service 介面

    @Autowired
    private ProductRepository productRepository; // 輔助配角：用來現做測試資料

    @Test
    @DisplayName("LINE Bot推薦測試 - 應成功過濾已下架商品、依權重排序且受到 LIMIT 1 限制")
    void getBotRecommendations_shouldFilterDeletedAndSortByPromoAndLimitToOne() {
        // =================================================================
        // 1. Arrange: 準備 3 筆除濕機測試資料，故意設下不同的狀態與權重
        // =================================================================

        // 🥇 商品 A：正常上架、有促銷 (is_promo = 1) -> 依照你的商業邏輯，這筆權重最高，預期要被撈出！
        Product promoProduct = new Product();
        promoProduct.setProductName("【限時特惠】豪華除濕機");
        promoProduct.setBrand("Panasonic");
        promoProduct.setCategory(ProductCategory.DEHUMIDIFIER); // 依你的 Enum 填入
        promoProduct.setPrice(15000);
        promoProduct.setStock(5);
        promoProduct.setIsPromo(false); // 促銷中！
        promoProduct.setIsDeleted(0);
        promoProduct.setProductSpec(new HashMap<>());
        productRepository.saveAndFlush(promoProduct);

        // 🥈 商品 B：正常上架、沒促銷 (is_promo = 0) -> 權重較低
        Product normalProduct = new Product();
        normalProduct.setProductName("一般家用除濕機");
        normalProduct.setBrand("Muji");
        normalProduct.setCategory(ProductCategory.DEHUMIDIFIER);
        normalProduct.setPrice(8000);
        normalProduct.setStock(10);
        normalProduct.setIsPromo(false); // 沒促銷
        normalProduct.setIsDeleted(0);
        normalProduct.setProductSpec(new HashMap<>());
        productRepository.saveAndFlush(normalProduct);

        // ❌ 商品 C：符合關鍵字，但早已下架 (is_deleted = 1) -> 鋼鐵防線，絕對不能撈出
        Product deletedProduct = new Product();
        deletedProduct.setProductName("絕版出清除濕機");
        deletedProduct.setBrand("Sharp");
        deletedProduct.setCategory(ProductCategory.DEHUMIDIFIER);
        deletedProduct.setPrice(5000);
        deletedProduct.setStock(0);
        deletedProduct.setIsPromo(true); // 就算是促銷，下架了就是不能賣
        deletedProduct.setIsDeleted(1); // 已下架
        deletedProduct.setProductSpec(new HashMap<>());
        productRepository.saveAndFlush(deletedProduct);

        // =================================================================
        // 2. Act: 模擬 LINE Bot 傳入條件，呼叫 Service 方法
        // =================================================================
        List<Product> result = productService.getBotRecommendations("除濕機", "DEHUMIDIFIER");

        // =================================================================
        // 3. Assert: 精準斷言驗證
        // =================================================================
        // 驗證 A：回傳不能為 null
        assertNotNull(result);

        // 驗證 B：因為 SQL 有寫 LIMIT 1，回傳的 List 長度必須精準等於 1
        assertEquals(1, result.size(), "LINE Bot 推薦商品數量應受到 LIMIT 1 限制");

        // 驗證 C：撈出來的那唯一一筆，必須是權重最高、且活著的「【限時特惠】豪華除濕機」
        Product recommended = result.get(0);
        assertEquals(0, recommended.getIsDeleted(), "推薦商品必須是未刪除的正常商品");
        assertEquals(1, recommended.getIsPromo(), "應該優先推薦正在促銷的商品");
        assertEquals("【限時特惠】豪華除濕機", recommended.getProductName());
    }
}
