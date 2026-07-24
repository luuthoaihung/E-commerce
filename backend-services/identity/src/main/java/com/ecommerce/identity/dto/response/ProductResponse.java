package com.ecommerce.identity.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private CategoryResponse category; 
}