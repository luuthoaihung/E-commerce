package com.ecommerce.identity.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    private String status; // CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}