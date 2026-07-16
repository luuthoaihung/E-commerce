package com.ecommerce.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Dòng này cực hay: Trường nào bị NULL (ví dụ khi lỗi thì không có result) sẽ tự động ẩn đi cho JSON sạch đẹp
public class ApiResponse<T> {
    @Builder.Default
    private int code = 1000; // 1000 là mặc định cho thành công
    private String message;
    private T result; // Dữ liệu trả về động (có thể là User, Token, String...)
}