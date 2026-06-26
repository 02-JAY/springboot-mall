package com.jay.springbootmall.dao;

import com.jay.springbootmall.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // 前台專用：依據 ID 且必須是未刪除（isDeleted = 0）的商品
    Optional<Product> findByProductIdAndIsDeleted(Integer productId, Integer isDeleted);
}
