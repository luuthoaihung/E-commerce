package com.ecommerce.identity.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String id;
    private List<CartItemResponse> items;
    private BigDecimal grandTotal; // Tổng tiền của toàn bộ giỏ hàng
}