package com.jay.springbootmall.dao;

import com.jay.springbootmall.model.Product;

public interface ProductDao {

    Product getProductById(Integer productId);

}
