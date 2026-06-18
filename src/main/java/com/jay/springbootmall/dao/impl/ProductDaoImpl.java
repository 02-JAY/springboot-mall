package com.jay.springbootmall.dao.impl;

import com.jay.springbootmall.dao.ProductDao;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.rowmapper.ProductRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
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
    public Product getProductById(Integer productId) {
        // 補齊這次設計的所有欄位：brand, version, is_promo, product_spec
        String sql = "SELECT product_id, product_name, brand, category, price, stock, " +
                "version, is_promo, product_spec, image_url, description, " +
                "created_date, last_modified_date " +
                "FROM product " +
                "WHERE product_id = :productId";

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        List<Product> productList = namedParameterJdbcOperations.query(sql, map, new ProductRowMapper());

        if (productList.size() > 0) {
            return productList.get(0);
        } else {
            return null;
        }
    }
}
