package com.ecommerce.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID") // 🌟 Bắt buộc username từ 4 ký tự trở lên
    String username;

    @Size(min = 6, message = "PASSWORD_INVALID") // 🌟 Bắt buộc password từ 6 ký tự trở lên
    String password;

    @NotBlank(message = "EMAIL_INVALID")
    @Email(message = "EMAIL_INVALID") 
    String email;
    
    String fullName;
}