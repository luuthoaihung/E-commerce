package com.ecommerce.identity.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    private String description;
    @NotNull(message = "Giá sản phẩm không được để trống")
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    @NotBlank(message = "Danh mục sản phẩm là bắt buộc")
    private String categoryId;
}
