package com.ecommerce.identity.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String name;
    private String description;
}