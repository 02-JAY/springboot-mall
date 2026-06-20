package com.jay.springbootmall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "修改商品請求參數")
public class UpdateProductDTO {

    @Schema(description = "商品名稱", example = " 香氛防潮袋")
    @NotBlank(message = "商品名稱不能為空")
    private String productName;

    @Schema(description = "品牌", example = "山水")
    @NotBlank(message = "品牌不能為空")
    private String brand;

    // 改成 Enum 型態，調整 Swagger 範例，並改用 @NotNull 驗證
    @Schema(description = "商品分類", example = "FRAGRANCE_PACK")
    @NotNull(message = "商品分類不能為 null") //  Enum 驗證要用 @NotNull
    private ProductCategory category;

    @Schema(description = "價格", example = "899")
    @NotNull(message = "價格不能為 null")
    @Min(value = 0, message = "價格不能小於 0")
    private Integer price;

    @Schema(description = "庫存數量", example = "100")
    @NotNull(message = "庫存不能為 null")
    @Min(value = 0, message = "庫存不能小於 0")
    private Integer stock;

    @Schema(description = "是否促銷中", example = "true")
    @NotNull(message = "是否促銷狀態不能為 null")
    private Boolean isPromo;

    @Schema(description = "商品規格 (JSON 格式)", example = "{\"type\": \"草本舒緩\", \"flavor\": \"南法薰衣草\", \"weight\": \"100g\", \"color\": \"purple\"}")
    @NotNull(message = "商品規格不能為 null")
    private Map<String, Object> productSpec;

    @Schema(description = "商品圖片網址", example = "https://example.com/images/protein.png")
    @NotBlank(message = "商品圖片網址不能為空")
    private String imageUrl;

    @Schema(description = "商品描述說明", example = "股帶有微酸、能瞬間提振精神的清爽爆發力")
    private String description; // 描述允許為空，所以不加 @NotBlank

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getIsPromo() {
        return isPromo;
    }

    public void setIsPromo(Boolean isPromo) {
        this.isPromo = isPromo;
    }

    public Map<String, Object> getProductSpec() {
        return productSpec;
    }

    public void setProductSpec(Map<String, Object> productSpec) {
        this.productSpec = productSpec;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}