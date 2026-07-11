package com.jay.springbootmall.product.repository;

import com.jay.springbootmall.product.model.Product;
import com.jay.springbootmall.product.model.ProductCategory;

import java.util.List;

public interface ProductJdbcRepository {

    List<Product> getProducts(String search, ProductCategory category, Integer minPrice, Integer maxPrice,
                              Boolean isPromotion, Integer page, Integer size, String orderBy, String sort);


    List<Product> getBotRecommendations(String keyword, ProductCategory category);

    List<Product> getAllProductsForAdmin(Integer isDeleted, Integer page, Integer size);
}
