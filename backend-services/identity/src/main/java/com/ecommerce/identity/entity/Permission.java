package com.ecommerce.identity.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    private String name; // Ví dụ: "CREATE_DATA", "READ_DATA", "DELETE_USER"
    private String description;
}