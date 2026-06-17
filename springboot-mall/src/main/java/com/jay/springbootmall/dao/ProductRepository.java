package com.jay.springbootmall.dao;

import com.jay.springbootmall.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // 透過 JPA 的命名規則，自動生成依分類查詢的 SQL
    List<Product> findByCategory(String category);
}
