package com.jay.springbootmall.controller;

import com.jay.springbootmall.model.Product;
import com.jay.springbootmall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "LINE Bot 專用 API", description = "提供給 Python LINE Bot 呼叫的智慧推薦介面")
@RestController
@RequestMapping("/api/v1/bot/products")
public class ProductBotController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "LINE Bot 智慧推薦商品", description = "接收 AI 提煉後的關鍵字與分類，跨長文本與 JSON 規格篩選出最相關的 3 筆商品。")
    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getBotRecommendations(
            @Parameter(description = "AI 提煉後的核心關鍵字（如：放鬆）", example = "放鬆")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "AI 預測的商品分類（如：FRAGRANCE_PACK）", example = "FRAGRANCE_PACK")
            @RequestParam(required = false) String category
    ) {
        // 呼叫 Service 執行推薦邏輯
        List<Product> recommendations = productService.getBotRecommendations(keyword, category);
        return ResponseEntity.ok(recommendations);
    }
}