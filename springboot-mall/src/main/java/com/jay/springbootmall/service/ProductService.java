package com.jay.springbootmall.service;

import com.jay.springbootmall.model.Product;

public interface ProductService {

    // 新增（走 JPA）
    Product createProduct(Product product);

    // 依 ID 查詢（走 JDBC）
    Product getProductById(Integer productId);

}
