package com.ecommerce.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại"),
    USER_EXISTED(1001, "Tài khoản này đã tồn tại trên hệ thống"),
    EMAIL_EXISTED(1002, "Email này đã được đăng ký sử dụng"),
    USERNAME_INVALID(1003, "Tài khoản phải có tối thiểu 3 ký tự"),
    PASSWORD_INVALID(1004, "Mật khẩu phải có ít nhất 6 ký tự"),
    EMAIL_INVALID(1005, "Email không đúng định dạng chuẩn (ví dụ: abc@gmail.com)"),
    USER_NOT_EXISTED(1006, "Tài khoản không tồn tại trên hệ thống"),
    UNAUTHENTICATED(1007, "Tài khoản hoặc mật khẩu không chính xác"),
    UNAUTHORIZED(1008, "Bạn không có quyền truy cập vào chức năng này"),
    INVALID_TOKEN(1009, "Token không hợp lệ hoặc đã đăng xuất", HttpStatus.UNAUTHORIZED),
    ;

    

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
        this.statusCode = HttpStatus.BAD_REQUEST;
    }

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    
}