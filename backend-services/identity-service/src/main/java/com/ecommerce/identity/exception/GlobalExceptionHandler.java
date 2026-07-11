package com.ecommerce.identity.exception;

import com.ecommerce.identity.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // Gom và xử lý lỗi của toàn bộ các Controller
public class GlobalExceptionHandler {

    // 1. Hứng lỗi hệ thống chung chung (Lỗi 500, NullPointer, sập DB...)
    @ExceptionHandler(value = Exception.class)
    
    public ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        exception.printStackTrace();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // 2. Hứng lỗi AppException do chính tụi mình chủ động ném ra (Lỗi nghiệp vụ)
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }
}