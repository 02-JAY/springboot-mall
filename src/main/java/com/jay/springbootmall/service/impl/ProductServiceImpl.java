package com.jay.springbootmall.service.impl;

import com.jay.springbootmall.dao.ProductDao;
import com.jay.springbootmall.dao.ProductRepository;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository; // 注入 JPA，專門搞定 Insert/Update 與搶購樂源鎖

    @Autowired
    private ProductDao productDao; // 注入你原本的 JDBC，負責未來的複雜 SQL 與 LINE Bot 查詢

    @Transactional
    @Override
    public Product createProduct(Product product) {
        // 呼叫 JPA 的 save() 方法，會自動 INSERT INTO product ...
        // 並且會自動處理 version 欄位初始化，同時將資料庫自增生成的 product_id 回填到物件中
        return productRepository.save(product);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Integer productId) {

        return productDao.getProductById(productId);
    }
}
