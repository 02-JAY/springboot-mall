package com.jay.springbootmall.service.impl;

import com.jay.springbootmall.dao.ProductDao;
import com.jay.springbootmall.dao.ProductRepository;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.UpdateProductDTO;
import com.jay.springbootmall.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository; // 注入 JPA，專門搞定 Insert/Update 與搶購樂源鎖

    @Autowired
    private ProductDao productDao; // 注入 JDBC，負責未來的複雜 SQL 與 LINE Bot 查詢

    // 區塊 A：後台管理系統 (Back-Office / Admin CMS)

    /**
     * 後台：上架/建立新商品
     */
    @Transactional
    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * 後台：修改商品商務資訊 (導入 DTO 與樂觀鎖控制)
     */
    @Override
    @Transactional
    public Product updateProduct(Integer productId, UpdateProductDTO updateProductDTO) {
        // 1. 先從資料庫查出帶有樂觀鎖版本（version）與建立時間的完整舊物件
        // 這裡必須用 productRepository (JPA) 查，才能讓這筆物件進入 JPA 的管理狀態，save 時才會動態比對 version 觸發樂觀鎖！
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("修改失敗：找不到該商品，ID: " + productId));

        // 2. 僅覆蓋「允許被修改」的欄位，防止重要安全參數被前端惡意覆蓋
        existingProduct.setProductName(updateProductDTO.getProductName());
        existingProduct.setBrand(updateProductDTO.getBrand());
        existingProduct.setCategory(updateProductDTO.getCategory());
        existingProduct.setPrice(updateProductDTO.getPrice());
        existingProduct.setStock(updateProductDTO.getStock());
        existingProduct.setIsPromo(updateProductDTO.getIsPromo());
        //existingProduct.setProductSpec(updateProductDTO.getProductSpec());
        existingProduct.setImageUrl(updateProductDTO.getImageUrl());
        existingProduct.setDescription(updateProductDTO.getDescription());

        // 備註：
        // 1. 不要去 setVersion，JPA 在 save() 時發現版本對齊，會自動幫你將資料庫的 version + 1。
        // 2. 不要去 setIsDeleted，商品的上架/下架狀態由專門的 DELETE API（軟刪除）控制，這裡不允許隨便被前端覆蓋。


        // 3.  精細處理 JSON 規格欄位 (Map 合併邏輯)
        if (updateProductDTO.getProductSpec() != null) {
            // 如果舊商品原本沒有任何規格，先幫他初始化一個 Map
            if (existingProduct.getProductSpec() == null) {
                existingProduct.setProductSpec(new HashMap<>());
            }

            // 增量合併：把前端送來的新規格，塞進舊有的 Map 裡（原本有的會覆蓋，原本有但前端沒傳的會被保留！）
            existingProduct.getProductSpec().putAll(updateProductDTO.getProductSpec());
        }


        // 4.將更新後的舊物件存回，這時樂觀鎖會完美觸發，防範同時修改的衝突
        return productRepository.save(existingProduct);
    }

    /**
     * 後台：下架商品（軟刪除）
     */
    @Override
    @Transactional
    public void deleteProductById(Integer productId) {
        // 1. 檢查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("刪除失敗：找不到該商品，ID: " + productId));

        // 2. 不執行實體刪除，而是把狀態改為 1
        product.setIsDeleted(1);

        // 3. 存回資料庫，這樣該商品在資料庫的 is_deleted 欄位就會變成 1,且樂觀鎖 version 會自動 +1
        productRepository.save(product);
    }

    /**
     * 後台：恢復已軟刪除的商品（軟刪除）
     */
    @Override
    @Transactional
    public void restoreProductById(Integer productId) {
        // 1. 1. 檢查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("恢復失敗：找不到該商品，ID: " + productId));

        // 2. 檢查是否刪除？
        if (product.getIsDeleted() == 0) {
            throw new RuntimeException("該商品目前處於正常上架狀態，不需恢復。");
        }

        // 3. 改回 0，JPA 會在 Transaction 結束時自動寫回資料庫，且樂觀鎖 version 會自動 +1
        product.setIsDeleted(0);
        productRepository.save(product);
    }



    // 區塊 B：前台購物網站與外部檢索 (Client Portal / LINE Bot API)

    /**
     * 常規查詢：依據 ID 獲取單筆商品完整詳情（走 JPA）
     */
    @Override
    public Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("查詢失敗：找不到該商品，ID: " + productId));
    }

    /**
     * 高階動態搜尋：支援多欄位模糊比對、分類過濾、價格區間篩選（走 JDBC 動態 SQL）
     */
    @Override
    public List<Product> getProducts(String search, String category, Integer minPrice, Integer maxPrice,
                                     Boolean isPromotion, Integer page, Integer size, String orderBy, String sort) {

        return productDao.getProducts(search, category, minPrice, maxPrice, isPromotion, page, size, orderBy, sort);
    }

    /**
     * LINE Bot 專用：依據精煉後的關鍵字與分類進行智慧推薦
     */
    @Override
    public List<Product> getBotRecommendations(String keyword, String category) {
        // 為了容錯，如果兩者都沒傳，就給個空字串避免噴錯
        String validKeyword = (keyword != null) ? keyword.trim() : "";
        String validCategory = (category != null) ? category.trim() : "";

        // 業務邏輯：直接交給 DAO 進行多維度 SQL 檢索
        return productDao.getBotRecommendations(validKeyword, validCategory);
    }
}
