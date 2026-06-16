package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer productId){
            Product product = productService.getProductById(productId);

            if(product!=null){
                return ResponseEntity.status(HttpStatus.OK).body(product);
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
