package com.ecommerce.identity.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    boolean authenticated; // true nếu đúng pass, false nếu sai
    String token;          // Tạm thời để trống, bước sau sẽ sinh token thật ở đây
}