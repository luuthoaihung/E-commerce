package com.ecommerce.identity.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String id;
    private ProductResponse product;
    private int quantity;
    private BigDecimal totalPrice; 
}