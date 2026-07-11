package com.ecommerce.identity.configuration;

import com.ecommerce.identity.entity.Role;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.repository.RoleRepository;
import com.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("all")
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    
    @Bean
ApplicationRunner applicationRunner(
        UserRepository userRepository, 
        RoleRepository roleRepository, 
        PasswordEncoder passwordEncoder) {
    return args -> {
        
        // 1. Kiểm tra và tạo Role ADMIN nếu chưa có
        Role adminRole = roleRepository.findById("ADMIN")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ADMIN");
                newRole.setDescription("Quản trị viên hệ thống");
                return roleRepository.save(newRole);
            });

        // 🌟 2. BỔ SUNG: Kiểm tra và tạo thêm Role USER nếu trong DB chưa có
        Role userRole = roleRepository.findById("USER")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("USER");
                newRole.setDescription("Người dùng thông thường");
                return roleRepository.save(newRole);
            });

        // 3. Kiểm tra và tạo tài khoản Admin mặc định
        if (userRepository.findByUsername("admin").isEmpty()) {
            var roles = new HashSet<Role>();
            roles.add(adminRole);

            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@gmail.com")
                    .fullName("Quản Trị Viên Hệ Thống")
                    .roles(roles)
                    .build();

            userRepository.save(adminUser);
            log.info("🌟 [HỆ THỐNG] Đã khởi tạo thành công tài khoản ADMIN mặc định (admin/admin123)!");
        }
    };
}
}