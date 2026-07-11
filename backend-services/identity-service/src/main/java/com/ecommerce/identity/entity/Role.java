package com.ecommerce.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")

public class Role {
    @Id
    @Column(length = 50)
    private String name; // Ví dụ: "ADMIN", "USER"
    private String description;

    
}