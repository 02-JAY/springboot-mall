package com.jay.springbootmall.service;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.UpdateProductDTO;

import java.util.List;

public interface ProductService {

    // 新增商品（走 JPA）
    Product createProduct(Product product);

    // 依照類別搜尋產品（走 JPA）
    List<Product> getProductsByCategory(String category);

    // 搜尋全部產品（走 JPA）
    List<Product> getAllProducts();

    // 後台修改商品：傳入要修改的 ID，以及帶有新資料的 Product 物件
    Product updateProduct(Integer productId, UpdateProductDTO updateProductDTO);

    // 後台刪除商品：只需要傳入 ID
    void deleteProductById(Integer productId);

    // 依 ID 查詢（走 JDBC）
    Product getProductById(Integer productId);

}
