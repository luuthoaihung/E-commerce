package com.ecommerce.identity.repository;

import com.ecommerce.identity.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    // Tự động sinh hàm kiểm tra xem tài khoản hoặc email đã tồn tại hay chưa
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
}