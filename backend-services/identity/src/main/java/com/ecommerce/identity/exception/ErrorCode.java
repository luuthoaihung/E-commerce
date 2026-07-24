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
    INVALID_PASSWORD(1010, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1011, "Mã OTP không chính xác", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1012, "Mã OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1013, "Danh mục đã tồn tại"),
    CATEGORY_NOT_FOUND(1014, "Không tìm thấy danh mục", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1015, "Không tìm thấy sản phẩm", HttpStatus.BAD_REQUEST),
    CART_NOT_FOUND(1016, "Không tìm thấy giỏ hàng", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(1017, "Không tìm thấy sản phẩm trong giỏ hàng", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(10018, "Sản phẩm không đủ số lượng trong kho", HttpStatus.BAD_REQUEST),
    CART_EMPTY(1019, "Giỏ hàng của bạn đang trống", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1020, "Không tìm thấy đơn hàng", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_ORDER(1021, "Không thể hủy đơn hàng này", HttpStatus.BAD_REQUEST),
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