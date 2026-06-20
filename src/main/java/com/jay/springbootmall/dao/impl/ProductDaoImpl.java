package com.jay.springbootmall.dao.impl;

import com.jay.springbootmall.dao.ProductDao;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.rowmapper.ProductRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class ProductDaoImpl implements ProductDao {
    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    public ProductDaoImpl(NamedParameterJdbcOperations namedParameterJdbcOperations) {
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
    }

    @Override
    public List<Product> getProducts(String search, String category, Integer minPrice, Integer maxPrice,
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

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND category = :category ");
            map.put("category", category);
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
        return namedParameterJdbcOperations.query(sql.toString(), map, new ProductRowMapper());
    }
}
