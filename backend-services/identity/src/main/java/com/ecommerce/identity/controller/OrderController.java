package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.OrderResponse;
import com.ecommerce.identity.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // API 1: Xem lịch sử tất cả đơn hàng của tôi
    @GetMapping
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getMyOrders())
                .build();
    }

    // API 2: Xem chi tiết một đơn hàng theo ID
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }
    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancelOrder(id))
                .build();
    }

    // 1. API tạo link thanh toán VNPay cho đơn hàng
    @PostMapping("/{id}/payment/vnpay")
    public ApiResponse<String> createVnPayPayment(@PathVariable String id, HttpServletRequest request) {
        String paymentUrl = orderService.createVnPayPayment(id, request);
        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .build();
    }

    // 2. API nhận kết quả trả về từ cổng VNPay (Return URL)
    @GetMapping("/vnpay-return")
    public ApiResponse<String> vnPayReturn(
            @RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String orderId = params.get("vnp_TxnRef");

        if ("00".equals(responseCode)) {
            // Thanh toán thành công -> Cập nhật trạng thái đơn hàng thành PAID
            orderService.updateOrderStatus(orderId, "PAID");
            return ApiResponse.<String>builder()
                    .result("Thanh toán thành công cho đơn hàng: " + orderId)
                    .build();
        } else {
            // Thanh toán thất bại hoặc bị hủy
            return ApiResponse.<String>builder()
                    .result("Thanh toán thất bại hoặc đã bị hủy!")
                    .build();
        }
        
    }
    // API 3: Đặt hàng (Thanh toán khi nhận hàng - COD)
    @PostMapping
    public ApiResponse<OrderResponse> createOrder() {
        OrderResponse orderResponse = orderService.createOrder();
        return ApiResponse.<OrderResponse>builder()
                .result(orderResponse)
                .message("Đặt hàng thành công")
                .build();
    }
    
}