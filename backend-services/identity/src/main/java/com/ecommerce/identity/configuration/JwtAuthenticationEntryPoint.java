package com.ecommerce.identity.configuration;

import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // 1. Lấy mã lỗi UNAUTHENTICATED (1007) đã cấu hình trong ErrorCode
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;

        // 2. Thiết lập HTTP Status và kiểu trả về là JSON cho Response
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3. Đóng gói dữ liệu theo đúng chuẩn ApiResponse của dự án
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // 4. Dùng ObjectMapper để biến Object Java thành chuỗi JSON và bắn về cho Client
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        
        // 5. Đẩy dữ liệu đi ngay lập tức
        response.flushBuffer();
    }
}