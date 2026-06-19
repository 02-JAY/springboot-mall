package com.jay.springbootmall.service.impl;

import com.jay.springbootmall.dao.ProductDao;
import com.jay.springbootmall.dao.ProductRepository;
import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.model.UpdateProductDTO;
import com.jay.springbootmall.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public Product updateProduct(Integer productId, UpdateProductDTO updateProductDTO) {
        // 📌 1. 先從資料庫查出帶有樂觀鎖版本（version）與建立時間的完整舊物件
        // 提示：這裡必須用 productRepository (JPA) 查，才能讓這筆物件進入 JPA 的管理狀態，save 時才會動態比對 version 觸發樂觀鎖！
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("修改失敗：找不到該商品，ID: " + productId));

        // 📌 2. 僅覆蓋「允許被修改」的欄位
        existingProduct.setProductName(updateProductDTO.getProductName());
        existingProduct.setBrand(updateProductDTO.getBrand());
        existingProduct.setCategory(updateProductDTO.getCategory());
        existingProduct.setPrice(updateProductDTO.getPrice());
        existingProduct.setStock(updateProductDTO.getStock());
        existingProduct.setIsPromo(updateProductDTO.getIsPromo());
        existingProduct.setProductSpec(updateProductDTO.getProductSpec());
        existingProduct.setImageUrl(updateProductDTO.getImageUrl());
        existingProduct.setDescription(updateProductDTO.getDescription());

        // 備註：
        // 1. 不要去 setVersion，JPA 在 save() 時發現版本對齊，會自動幫你將資料庫的 version + 1。
        // 2. 不要去 setIsDeleted，商品的上架/下架狀態由專門的 DELETE API（軟刪除）控制，這裡不允許隨便被前端覆蓋。

        // 將更新後的舊物件存回，這時樂觀鎖會完美觸發，防範同時修改的衝突
        return productRepository.save(existingProduct);
    }

    // 🌟 後台刪除商品 (改寫為軟刪除)
    @Override
    public void deleteProductById(Integer productId) {
        // 1. 檢查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("刪除失敗：找不到該商品，ID: " + productId));

        // 2. 核心精髓：不執行實體刪除，而是把狀態改為 1
        product.setIsDeleted(1);

        // 3. 存回資料庫，這樣該商品在資料庫的 is_deleted 欄位就會變成 1
        productRepository.save(product);
    }


    @Override
    public Product getProductById(Integer productId) {
        Product product = productDao.getProductById(productId);

        // 在 Service 層做檢查：如果 JDBC 撈出來是 null，直接噴 404 異常
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到該商品，ID: " + productId);
        }

        // 資料正常就回傳，Controller 就能直接用
        return product;
    }
}
