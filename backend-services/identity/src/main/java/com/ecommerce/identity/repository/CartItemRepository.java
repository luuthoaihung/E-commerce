package com.ecommerce.identity.repository;

import com.ecommerce.identity.entity.Cart;
import com.ecommerce.identity.entity.CartItem;
import com.ecommerce.identity.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}