package com.jay.springbootmall.product.repository.impl;

import com.jay.springbootmall.product.repository.ProductJdbcRepository;
import com.jay.springbootmall.product.model.Product;
import com.jay.springbootmall.product.model.ProductCategory;
import com.jay.springbootmall.product.repository.ProductRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class ProductJdbcRepositoryImpl implements ProductJdbcRepository {
    // 1. 將所有依賴都宣告為 private final（不可變，更安全）
    private final NamedParameterJdbcOperations namedParameterJdbcOperations;
    private final ProductRowMapper productRowMapper;

    // 2. 統一用同一個建構子注入！連 @Autowired 都不用寫，Spring 會自動幫你把這兩個 Bean 塞進來
    public ProductJdbcRepositoryImpl(NamedParameterJdbcOperations namedParameterJdbcOperations,
                                     ProductRowMapper productRowMapper) {
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
        this.productRowMapper = productRowMapper;
    }

    @Override
    public List<Product> getProducts(String search, ProductCategory category, Integer minPrice, Integer maxPrice,
                                     Boolean isPromotion, Integer page, Integer size, String orderBy, String sort) {

        StringBuilder sql = new StringBuilder(
                "SELECT product_id, product_name, brand, category, price, stock, " +
                        "version, is_promo, product_spec, image_url, description, is_deleted, " +
                        "created_date, last_modified_date " +
                        "FROM product " +
                        "WHERE 1=1 "
        );

        Map<String, Object> map = new HashMap<>();

        // 1. 前台商城「絕對不能」撈出已被軟刪除的商品
        sql.append("AND is_deleted = 0 ");

        // 2. 動態條件篩選
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (product_name LIKE :search OR brand LIKE :search) "); // 順便連品牌一起模糊搜尋
            map.put("search", "%" + search + "%");
        }

//        if (category != null && !category.trim().isEmpty()) {
//            sql.append("AND category = :category ");
//            map.put("category", category);
//        }
        // 修改條件：因為變成 Enum，只要判斷是否為 null
        if (category != null) {
            sql.append("AND category = :category ");
            map.put("category", category.name()); // 用 .name() 轉成字串（如 "FRAGRANCE_PACK"）
        }

        if (minPrice != null) {
            sql.append("AND price >= :minPrice ");
            map.put("minPrice", minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND price <= :maxPrice ");
            map.put("maxPrice", maxPrice);
        }

        // 新增條件 E：是否只看促銷商品
        if (isPromotion != null && isPromotion) {
            sql.append("AND is_promo = 1 "); // 假設 1 代表正在特價/促銷
        }

        // 3. 動態排序（防止 SQL 注入的安全白名單檢查）
        // 檢查前端傳來的排序欄位是不是合法的資料庫欄位，若不合法則強制使用預設值 created_date
        String validOrderBy = "created_date";
        if ("price".equalsIgnoreCase(orderBy) || "product_id".equalsIgnoreCase(orderBy) || "last_modified_date".equalsIgnoreCase(orderBy)) {
            validOrderBy = orderBy;
        }

        // 檢查排序方向
        String validSort = "DESC";
        if ("asc".equalsIgnoreCase(sort)) {
            validSort = "ASC";
        }

        // 拼接排序字串（注意前後空格）
        sql.append("ORDER BY ").append(validOrderBy).append(" ").append(validSort).append(" ");

        // 4. JDBC 分頁處理 (LIMIT 與 OFFSET)
        sql.append("LIMIT :size OFFSET :offset ");
        map.put("size", size);
        map.put("offset", page * size); // 第一頁 page=0, offset=0; 第二頁 page=1, offset=10

        // 5. 執行查詢
        return namedParameterJdbcOperations.query(sql.toString(), map, productRowMapper);
    }

    @Override
    public List<Product> getBotRecommendations(String keyword, ProductCategory category) {
        // 基礎 SQL：確保撈出所有必要欄位，且商品必須是「未刪除」的正常商品
        StringBuilder sql = new StringBuilder(
                "SELECT product_id, product_name, brand, category, price, stock, " +
                        "version, is_promo, product_spec, image_url, description, is_deleted, " +
                        "created_date, last_modified_date " +
                        "FROM product " +
                        "WHERE is_deleted = 0 "
        );

        Map<String, Object> map = new HashMap<>();

        // 🌟 動態條件拼接
        // 1. 如果 AI 有成功預測出精準的分類，直接鎖定分類查詢
        if (category != null ) {
            sql.append("AND category = :category ");
            map.put("category", category.name());
        }

        // 2. 如果有核心關鍵字，進行多維度（品名、描述、JSON規格）模糊比對
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND ( ")
                    .append("    product_name LIKE :keyword ")
                    .append("    OR description LIKE :keyword ")
                    .append("    OR LOWER(product_spec) LIKE :keyword ")
                    .append(") ");
            map.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        // 商業推薦權重：優先推薦正在促銷（is_promo=1）的商品，其次才是最新上架的
        sql.append("ORDER BY is_promo DESC, created_date DESC ");

        // 限制只拿 3 筆，完美符合 LINE Carousel 視窗版面限制限制
        sql.append("LIMIT 1");

        // 執行查詢並丟給你的 RowMapper 自動解析
        return namedParameterJdbcOperations.query(sql.toString(), map, productRowMapper);
    }

    @Override
    public List<Product> getAllProductsForAdmin(Integer isDeleted, Integer page, Integer size) {
        StringBuilder sql = new StringBuilder(
                "SELECT product_id, product_name, brand, category, price, stock, " +
                        "version, is_promo, product_spec, image_url, description, is_deleted, " +
                        "created_date, last_modified_date " +
                        "FROM product " +
                        "WHERE 1=1 " // 方便後續動態拼接 AND
        );

        Map<String, Object> map = new HashMap<>();

        // 核心動態判斷：
        // 管理員如果傳 0，就查上架中；傳 1，就查下架倉庫；如果不傳(null)，就代表全部都看！
        if (isDeleted != null) {
            sql.append("AND is_deleted = :isDeleted ");
            map.put("isDeleted", isDeleted);
        }

        // 後台清單排序邏輯：最新修改或最新上架的商品排在最前面
        sql.append("ORDER BY last_modified_date DESC, product_id DESC ");

        // 分頁處理
        sql.append("LIMIT :size OFFSET :offset ");
        map.put("size", size);
        map.put("offset", page * size);

        return namedParameterJdbcOperations.query(sql.toString(), map, productRowMapper);
    }


}
