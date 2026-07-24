package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.CartItemRequest;
import com.ecommerce.identity.dto.response.CartItemResponse;
import com.ecommerce.identity.dto.response.CartResponse;
import com.ecommerce.identity.dto.response.ProductResponse;
import com.ecommerce.identity.entity.Cart;
import com.ecommerce.identity.entity.CartItem;
import com.ecommerce.identity.entity.Product;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.mapper.ProductMapper;
import com.ecommerce.identity.repository.CartItemRepository;
import com.ecommerce.identity.repository.CartRepository;
import com.ecommerce.identity.repository.ProductRepository;
import com.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper; // 🌟 Thêm ProductMapper vào đây

    // Lấy giỏ hàng của user hiện tại (dựa vào token đăng nhập)
    private Cart getCurrentUserCart() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    public CartItemResponse addToCart(CartItemRequest request) {
        Cart cart = getCurrentUserCart();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem savedItem = cartItemRepository.save(cartItem);

        // Map sang ProductResponse (nếu anh đã có mapper hoặc build thủ công)
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();

        // Tính tổng giá tiền = price * quantity
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(savedItem.getQuantity()));

        // Trả về CartItemResponse đã có sẵn
        return CartItemResponse.builder()
                .id(savedItem.getId())
                .product(productResponse)
                .quantity(savedItem.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }

    public CartItem updateCartItemQuantity(String cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public void removeCartItem(String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItemRepository.delete(cartItem);
    }

    public CartResponse getMyCart() {
        // 🌟 Tái sử dụng luôn hàm getCurrentUserCart() để code gọn gàng, tránh lặp lại
        Cart cart = getCurrentUserCart();

        // Chuyển đổi CartItem sang CartItemResponse và tính tổng tiền
        BigDecimal grandTotal = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                BigDecimal itemTotal = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                
                grandTotal = grandTotal.add(itemTotal);

                itemResponses.add(CartItemResponse.builder()
                        .id(item.getId())
                        .product(productMapper.toProductResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .totalPrice(itemTotal)
                        .build());
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .grandTotal(grandTotal)
                .build();
    }
}