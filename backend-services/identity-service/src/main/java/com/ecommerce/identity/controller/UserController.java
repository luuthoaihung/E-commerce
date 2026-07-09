package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/secret-data")
    public ApiResponse<String> getSecretData() {
        return ApiResponse.<String>builder()
                .message("Chúc mừng! Bạn đã vượt qua hàng rào bảo mật thành công.")
                .result("Đây là dữ liệu tối mật từ Backend: 👑 Mã kho báu 123456")
                .build();
    }
}