package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.request.CartItemRequest;
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.CartItemResponse;
import com.ecommerce.identity.dto.response.CartResponse;
import com.ecommerce.identity.entity.CartItem;
import com.ecommerce.identity.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
public ApiResponse<CartResponse> getMyCart() { // 🌟 Sửa List<CartItem> thành CartResponse
    return ApiResponse.<CartResponse>builder()
            .result(cartService.getMyCart())
            .build();
}

    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addToCart(@RequestBody @Valid CartItemRequest request) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.addToCart(request))
                .build();
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartItem> updateCartItem(@PathVariable String id, @RequestParam int quantity) {
        return ApiResponse.<CartItem>builder()
                .result(cartService.updateCartItemQuantity(id, quantity))
                .build();
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<String> removeCartItem(@PathVariable String id) {
        cartService.removeCartItem(id);
        return ApiResponse.<String>builder()
                .result("Cart item has been removed successfully")
                .build();
    }
}