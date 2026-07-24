package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.request.AuthenticationRequest;
import com.ecommerce.identity.dto.request.LogoutRequest;
import com.ecommerce.identity.dto.request.RefreshTokenRequest;
import com.ecommerce.identity.dto.request.UserCreationRequest; 
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.AuthenticationResponse;
import com.ecommerce.identity.service.AuthService;
import com.ecommerce.identity.service.UserService; 
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService; 

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder().result(authService.authenticate(request)).build();
    }

    // 🌟 2. Thêm endpoint đăng ký tài khoản công khai
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<String>builder()
                .result(userService.register(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder().result(authService.refreshToken(request)).build();
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ApiResponse.<String>builder()
                .result("Mã OTP đã được gửi về email của bạn!")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @RequestParam String email, 
            @RequestParam String otp, 
            @RequestParam @Size(min = 6, message = "INVALID_PASSWORD") String newPassword) {
        userService.resetPassword(email, otp, newPassword);
        return ApiResponse.<String>builder()
                .result("Đặt lại mật khẩu thành công!")
                .build();
    }
}