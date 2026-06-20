package com.jay.springbootmall.dao;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.ProductCategory;

import java.util.List;

public interface ProductDao {

    List<Product> getProducts(String search, ProductCategory category, Integer minPrice, Integer maxPrice,
                              Boolean isPromotion, Integer page, Integer size, String orderBy, String sort);


    List<Product> getBotRecommendations(String keyword, String category);
}
