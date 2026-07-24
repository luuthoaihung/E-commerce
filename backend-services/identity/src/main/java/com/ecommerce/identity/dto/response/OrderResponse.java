package com.ecommerce.identity.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> orderItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String id;
        private ProductResponse product;
        private int quantity;
        private BigDecimal price;
    }
}