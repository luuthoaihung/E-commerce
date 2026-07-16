package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.AuthenticationRequest;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private AuthenticationRequest request;

    @BeforeEach
    void initData() {
        user = User.builder().username("thanh").password("encodedPassword").build();
        request = AuthenticationRequest.builder()
                .username("thanh")
                .password("password123")
                .build();

        // Thêm dòng này để fix lỗi và kiểm tra luôn sự khởi tạo
        assertNotNull(authService); 
        
        ReflectionTestUtils.setField(authService, "SIGNER_KEY", "your-secret-key-that-is-at-least-32-characters-long");
    }

    @Test
    void login_success() {
        // GIVEN: Giả lập database tìm thấy user và password khớp
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // WHEN: Gọi service
        var response = authService.authenticate(request);

        // THEN: Kiểm tra kết quả
        assertNotNull(response);
        // Kiểm tra xem token có được trả về không
        // assertTrue(response.isAuthenticated()); // Hoặc kiểm tra thuộc tính tương ứng
    }

    @Test
    void login_fail_user_not_found() {
        // GIVEN: Giả lập database không tìm thấy user
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // WHEN & THEN: Kiểm tra xem có ném ra lỗi AppException (hoặc RuntimeException) như mong đợi không
        assertThrows(RuntimeException.class, () -> authService.authenticate(request));
    }
}