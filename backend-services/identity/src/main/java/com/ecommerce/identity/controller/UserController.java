package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.request.ChangePasswordRequest;
import com.ecommerce.identity.dto.request.ProfileRequest;
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.ProfileResponse;
import com.ecommerce.identity.dto.response.UserResponse;
import com.ecommerce.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;                   
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; 

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor 
public class UserController {

    private final UserService userService; 

    @GetMapping("/my-info") 
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    // API cập nhật thông tin cá nhân
    @PutMapping("/edit-profile")
    public ApiResponse<ProfileResponse> updateProfile(@RequestBody ProfileRequest request) {
        return ApiResponse.<ProfileResponse>builder()
                .result(userService.updateProfile(request))
                .build();
    }
    
    @GetMapping 
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/secret-data") 
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> getSecretData() {
        return ApiResponse.<String>builder()
                .message("Chúc mừng! Bạn đã vượt qua hàng rào bảo mật thành công")
                .result("Đây là dữ liệu tối mật từ Backend: 👑 Mã kho báu 12345")
                .build();
    }
    
    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.<String>builder()
                .result("Đổi mật khẩu thành công!")
                .build();
    }
    
}