package com.ecommerce.identity.entity;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Table(name = "users") // Khớp với tên bảng trong SQL Server
@Getter
@Setter
@NoArgsConstructor  // 🌟 THÊM DÒNG NÀY: Tạo constructor không tham số cho Hibernate
@AllArgsConstructor // 🌟 THÊM DÒNG NÀY: Tạo constructor đầy đủ tham số để @Builder không bị lỗi
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false) // Map trực tiếp vào cột password_hash bạn tạo hôm trước
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<Role> roles;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    // --- GETTER & SETTER (Bắt buộc phải có để Spring Boot đọc/ghi dữ liệu) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<Role> getRoles() {
    return this.roles;
    }
    public void setRoles(Set<Role> roles) {
    this.roles = roles;
    }

    String otp;
    LocalDateTime otpExpiryDate;
    

}