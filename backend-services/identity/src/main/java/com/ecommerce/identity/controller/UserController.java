package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.UserResponse; // 🌟 Import DTO mới
import com.ecommerce.identity.service.UserService;       // 🌟 Import Service để gọi logic
import lombok.RequiredArgsConstructor;                   // 🌟 Thêm Lombok để tự động kết nối Service
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; // 🌟 Import thư viện List của Java

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // 🌟 Injection tự động: Giúp nạp UserService vào mà không cần dùng @Autowired
public class UserController {

    private final UserService userService; // 🌟 Khai báo Service để sử dụng dưới hàm getAllUsers

    @GetMapping("/my-info") // 🌟 Đường dẫn đầy đủ sẽ là: GET http://localhost:8081/api/users/my-info
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    // 1. API lấy danh sách User (Tụi mình vừa làm xong)
    @GetMapping // 🌟 Không viết thêm gì ở đây -> Đường dẫn chuẩn sẽ là: /api/users
    @PreAuthorize("hasRole('ADMIN')") // 🔒 Chỉ có ADMIN mới được gọi
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }


    // 2. API mật mã tĩnh cũ của bạn (giữ nguyên để test)
    @GetMapping("/secret-data") // Đường dẫn phụ -> /api/users/secret-data
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> getSecretData() {
        return ApiResponse.<String>builder()
                .message("Chúc mừng! Bạn đã vượt qua hàng rào bảo mật thành công")
                .result("Đây là dữ liệu tối mật từ Backend: 👑 Mã kho báu 12345")
                .build();
    }
}