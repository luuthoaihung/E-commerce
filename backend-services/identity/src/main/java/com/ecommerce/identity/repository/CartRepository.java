package com.ecommerce.identity.repository;

import com.ecommerce.identity.entity.Cart;
import com.ecommerce.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUser(User user);
}