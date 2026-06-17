package com.jay.springbootmall.service;

import com.jay.springbootmall.model.Product;

import java.util.List;

public interface ProductService {

    // 新增商品（走 JPA）
    Product createProduct(Product product);

    // 依照類別搜尋產品（走 JPA）
    List<Product> getProductsByCategory(String category);

    // 搜尋全部產品（走 JPA）
    List<Product> getAllProducts();

    // 依 ID 查詢（走 JDBC）
    Product getProductById(Integer productId);

}
