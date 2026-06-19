package com.jay.springbootmall.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.springbootmall.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ProductRowMapper implements RowMapper<Product> {

    // 用於將資料庫的 JSON 字串轉換為 Java 的 Map
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product();

        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setBrand(rs.getString("brand"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getInt("price"));
        product.setStock(rs.getInt("stock"));
        product.setVersion(rs.getInt("version"));
        product.setIsPromo(rs.getBoolean("is_promo"));
        product.setImageUrl(rs.getString("image_url"));
        product.setDescription(rs.getString("description"));
        product.setIsDeleted(rs.getInt("is_deleted"));
        product.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        product.setLastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime());

        // 核心：處理 MySQL 的 JSON 欄位轉換為 Map<String, Object>
        String jsonSpec = rs.getString("product_spec");
        if (jsonSpec != null) {
            try {
                Map<String, Object> specMap = objectMapper.readValue(jsonSpec, new TypeReference<Map<String, Object>>() {});
                product.setProductSpec(specMap);
            } catch (Exception e) {
                // 如果轉換失敗，先印出 log，並塞個空 Map 防止 NullPointerException
                e.printStackTrace();
                product.setProductSpec(Map.of());
            }
        }

        return product;
    }
}
