package com.jay.springbootmall.product.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.springbootmall.product.model.Product;
import com.jay.springbootmall.product.model.ProductCategory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductRowMapper implements RowMapper<Product> {

    // 用於將資料庫的 JSON 字串轉換為 Java 的 Map
    //private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectMapper objectMapper;

    // 透過建構子注入 Spring 全域的 ObjectMapper
    public ProductRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product();

        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setBrand(rs.getString("brand"));
        //product.setCategory(rs.getString("category"));
        // 將資料庫字串轉為 Java Enum
        String categoryStr = rs.getString("category");
        if (categoryStr != null) {
            product.setCategory(ProductCategory.valueOf(categoryStr));
        }
        product.setPrice(rs.getInt("price"));
        product.setStock(rs.getInt("stock"));
        product.setVersion(rs.getInt("version"));
        product.setIsPromo(rs.getBoolean("is_promo"));
        product.setImageUrl(rs.getString("image_url"));
        product.setDescription(rs.getString("description"));
        product.setIsDeleted(rs.getInt("is_deleted"));
        //product.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        //product.setLastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime());

        // 修正 1：防止時間欄位為 NULL 導致的 NPE
        Timestamp createdTimestamp = rs.getTimestamp("created_date");
        if (createdTimestamp != null) {
            product.setCreatedDate(createdTimestamp.toLocalDateTime());
        }

        Timestamp modifiedTimestamp = rs.getTimestamp("last_modified_date");
        if (modifiedTimestamp != null) {
            product.setLastModifiedDate(modifiedTimestamp.toLocalDateTime());
        }

//        // 核心：處理 MySQL 的 JSON 欄位轉換為 Map<String, Object>
//        String jsonSpec = rs.getString("product_spec");
//        if (jsonSpec != null) {
//            try {
//                Map<String, Object> specMap = objectMapper.readValue(jsonSpec, new TypeReference<Map<String, Object>>() {});
//                product.setProductSpec(specMap);
//            } catch (Exception e) {
//                // 如果轉換失敗，先印出 log，並塞個空 Map 防止 NullPointerException
//                e.printStackTrace();
//                product.setProductSpec(Map.of());
//            }
//        }

        // 處理 JSON 欄位
        String jsonSpec = rs.getString("product_spec");
        if (jsonSpec != null) {
            try {
                Map<String, Object> specMap = objectMapper.readValue(jsonSpec, new TypeReference<Map<String, Object>>() {});
                product.setProductSpec(specMap);
            } catch (Exception e) {
                e.printStackTrace();
                // 修正 3：使用可變的 HashMap 防止後續操作報錯
                product.setProductSpec(new HashMap<>());
            }
        } else {
            product.setProductSpec(new HashMap<>());
        }

        return product;
    }
}
