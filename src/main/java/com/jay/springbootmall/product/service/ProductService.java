package com.jay.springbootmall.product.service;

import com.jay.springbootmall.product.model.Product;
import com.jay.springbootmall.product.model.ProductCategory;
import com.jay.springbootmall.product.dto.ProductRequestDTO;

import java.util.List;

public interface ProductService {

    // ========================================================================
    // 🏢 區塊 A：後台管理系統 (Back-Office / Admin CMS)
    // 職責：處理高安全性、防衝突的商品異動（CUD），核心技術走 JPA 狀態託管
    // ========================================================================

    /**
     * 後台：上架/建立新商品
     * @param product 完整的商品實體
     * @return 建立成功並回填 ID 的商品物件
     */
    Product createProduct(ProductRequestDTO productRequestDTO);

    /**
     * 後台（Admin）：依 ID 查詢單一商品（無論上下架都能查到）
     */
    Product getProductByIdForAdmin(Integer productId);

    /**
     * 後台：獲取所有商品列表（含上下架狀況）
     * * @param isDeleted 過濾刪除狀態：0 代表未刪除（上架中），1 代表已刪除（下架），傳 null 則不限制
     * @param page      分頁頁碼（從 0 開始）
     * @param size      每頁顯示的商品筆數
     * @return 包含分頁商品資料的列表集合
     */
    List<Product> getAllProductsForAdmin(Integer isDeleted, Integer page, Integer size);

    /**
     * 後台：修改商品商務資訊
     * @param productId 要修改的商品 ID
     * @param ProductRequestDTO 允許被前端覆蓋的修改欄位資料
     * @return 更新成功且樂觀鎖版本（version）遞增後的商品物件
     */
    Product updateProduct(Integer productId, ProductRequestDTO productRequestDTO);

    /**
     * 後台：下架商品（軟刪除）
     * @param productId 要下架的商品 ID
     */
    void deleteProductById(Integer productId);

    /**
     * 後台：恢復已軟刪除的商品（軟刪除）
     * @param productId 要上架的商品 ID
     */
    void restoreProductById(Integer productId);


    // ========================================================================
    // 🛒 區塊 B：前台購物網站與外部檢索 (Client Portal / LINE Bot API)
    // 職責：處理各式各樣的商品資料數據撈取（Read），依需求劃分 JPA 精準查詢與 JDBC 動態搜尋
    // ========================================================================

    /**
     * 精準查詢：依據 ID 獲取單筆商品完整詳情（走 JPA）
     * @param productId 商品識別碼
     * @return 商品實體物件
     */
    //Product getProductById(Integer productId);
    Product getProductByIdForClient(Integer productId);

    /**
     * 高階動態搜尋：支援多欄位模糊比對、分類過濾、價格區間篩選（走 JDBC 動態 SQL）
     * @param search 關鍵字（模糊比對商品名稱或品牌）
     * @param category 商品類別名稱
     * @param minPrice 最低價格限制限制
     * @param maxPrice 最高價格限制限制
     * @return 符合動態篩選條件的商品清單
     */
    List<Product> getProducts(String search, ProductCategory category, Integer minPrice, Integer maxPrice,
                              Boolean isPromotion, Integer page, Integer size, String orderBy, String sort);


    /**
     * LINE Bot 專用：依據精煉後的關鍵字與分類進行智慧推薦
     */
    List<Product> getBotRecommendations(String keyword, ProductCategory category);


}
