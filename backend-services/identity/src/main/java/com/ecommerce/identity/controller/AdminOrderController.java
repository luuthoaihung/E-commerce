package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.request.OrderStatusUpdateRequest;
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.OrderResponse;
import com.ecommerce.identity.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ tài khoản có Role ADMIN mới được truy cập các API này
public class AdminOrderController {

    private final OrderService orderService;

    // API 1: Lấy danh sách toàn bộ đơn hàng của tất cả khách hàng
    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrders())
                .build();
    }

    // API 2: Cập nhật trạng thái của 1 đơn hàng bất kỳ
    @PutMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrderStatus(id, request.getStatus()))
                .build();
    }
}