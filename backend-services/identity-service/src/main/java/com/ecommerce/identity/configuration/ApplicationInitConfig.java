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
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // 1. Kiểm tra xem tài khoản "admin" đã tồn tại trong DB chưa
            if (userRepository.findByUsername("admin").isEmpty()) {
                
                // 2. Kiểm tra và tạo Role ADMIN nếu trong DB chưa có
                Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ADMIN");
                        newRole.setDescription("Quản trị viên hệ thống");
                        return roleRepository.save(newRole);
                    });
                var roles = new HashSet<Role>();
                roles.add(adminRole);

                // 3. Tạo tài khoản Admin mặc định
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123")) // Mật khẩu sẽ là admin123
                        .email("admin@gmail.com")
                        .roles(roles)
                        .build();

                userRepository.save(adminUser);
                log.info("🌟 [HỆ THỐNG] Đã khởi tạo thành công tài khoản ADMIN mặc định (admin/admin123)!");
            }
        };
    }
}