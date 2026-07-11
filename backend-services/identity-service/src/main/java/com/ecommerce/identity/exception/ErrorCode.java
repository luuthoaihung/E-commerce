package com.ecommerce.identity.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại"),
    USER_EXISTED(1001, "Tài khoản này đã tồn tại trên hệ thống"),
    EMAIL_EXISTED(1002, "Email này đã được đăng ký sử dụng"),
    USERNAME_INVALID(1003, "Tài khoản phải có tối thiểu 3 ký tự"),
    PASSWORD_INVALID(1004, "Mật khẩu phải có ít nhất 6 ký tự"),
    EMAIL_INVALID(1005, "Email không đúng định dạng chuẩn (ví dụ: abc@gmail.com)"),
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;

    
}