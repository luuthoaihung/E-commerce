package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.response.OrderResponse;
import com.ecommerce.identity.entity.*;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.mapper.ProductMapper;
import com.ecommerce.identity.repository.*;
import com.ecommerce.identity.util.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    // Helper lấy user đang đăng nhập hiện tại
    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public OrderResponse createOrder() {
        // 1. Lấy user đang đăng nhập
        User user = getCurrentUser();

        // 2. Lấy giỏ hàng của user
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        // 3. Khởi tạo Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Duyệt qua từng sản phẩm trong giỏ hàng để kiểm tra tồn kho và tạo OrderItem
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Kiểm tra số lượng tồn kho
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            // Trừ số lượng kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tính tiền item
            BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemPrice);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Lưu Order vào database
        Order savedOrder = orderRepository.save(order);

        // 5. Xóa sạch giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteAll(cart.getCartItems());

        // 6. Map dữ liệu trả về DTO
        List<OrderResponse.OrderItemResponse> itemResponses = savedOrder.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .product(productMapper.toProductResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus())
                .orderDate(savedOrder.getOrderDate())
                .orderItems(itemResponses)
                .build();
    }

    // 1. Xem tất cả đơn hàng của user đang đăng nhập
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream().map(order -> {
            List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(item -> OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .product(productMapper.toProductResponse(item.getProduct()))
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();

            return OrderResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .orderDate(order.getOrderDate())
                    .orderItems(itemResponses)
                    .build();
        }).toList();
    }

    // 2. Xem chi tiết 1 đơn hàng theo ID
    public OrderResponse getOrderById(String orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra bảo mật: Đảm bảo đơn hàng thuộc về user hiện tại
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .product(productMapper.toProductResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .orderItems(itemResponses)
                .build();
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        User user = getCurrentUser();
        
        // 1. Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // 2. Kiểm tra bảo mật (Đơn hàng phải thuộc về user hiện tại)
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Chỉ cho phép hủy khi đơn hàng đang ở trạng thái PENDING
        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_ORDER); // Hoặc mã lỗi tùy chỉnh
        }

        // 4. Cập nhật trạng thái đơn hàng thành CANCELLED
        order.setStatus("CANCELLED");

        // 5. Hoàn lại số lượng tồn kho cho từng sản phẩm trong đơn
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        Order savedOrder = orderRepository.save(order);

        // 6. Map dữ liệu trả về DTO
        List<OrderResponse.OrderItemResponse> itemResponses = savedOrder.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .product(productMapper.toProductResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus())
                .orderDate(savedOrder.getOrderDate())
                .orderItems(itemResponses)
                .build();
    }
    // 1. Admin lấy toàn bộ danh sách đơn hàng của tất cả khách hàng
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(item -> OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .product(productMapper.toProductResponse(item.getProduct()))
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();

            return OrderResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .orderDate(order.getOrderDate())
                    .orderItems(itemResponses)
                    .build();
        }).toList();
    }
    // 2. Admin cập nhật trạng thái đơn hàng
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Nếu đơn hàng chuyển sang trạng thái CANCELLED từ phía Admin, cũng cần hoàn tồn kho
        if ("CANCELLED".equalsIgnoreCase(newStatus) && !"CANCELLED".equalsIgnoreCase(order.getStatus())) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus.toUpperCase());
        Order updatedOrder = orderRepository.save(order);

        List<OrderResponse.OrderItemResponse> itemResponses = updatedOrder.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .product(productMapper.toProductResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(updatedOrder.getId())
                .totalAmount(updatedOrder.getTotalAmount())
                .status(updatedOrder.getStatus())
                .orderDate(updatedOrder.getOrderDate())
                .orderItems(itemResponses)
                .build();
    }

    // thanh toán tực tuyến
    @Value("${vnp.payUrl}")
    private String vnpPayUrl;
    @Value("${vnp.tmnCode}")
    private String vnpTmnCode;
    @Value("${vnp.hashSecret}")
    private String vnpHashSecret;
    @Value("${vnp.returnUrl}")
    private String vnpReturnUrl;

    public String createVnPayPayment(String orderId, HttpServletRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        long amount = order.getTotalAmount().longValue() * 100;
        String vnpTxnRef = orderId;
        String vnpIpAddr = "127.0.0.1";

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "ThanhToanDonHang" + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", vnpIpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // --- ĐOẠN CODE CHUẨN TỪ VNPAY ---
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpPayUrl + "?" + queryUrl;
        
        System.out.println("VNPAY URL: " + paymentUrl);
        return paymentUrl;
    }

}