package com.jay.springbootmall.dao;

import com.jay.springbootmall.model.Product;

import java.util.List;

public interface ProductDao {

    List<Product> getProducts(String search, String category, Integer minPrice, Integer maxPrice,
                              Boolean isPromotion, Integer page, Integer size, String orderBy, String sort);

}
