-- ==========================================
-- SCRIPT KHỞI TẠO DATABASE IDENTITY (JAVA)
-- Cập nhật lần cuối: 08/07/2026
-- ==========================================

-- 1. Tạo Database nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Ecommerce_IdentityDb')
BEGIN
    CREATE DATABASE Ecommerce_IdentityDb;
END
GO

USE Ecommerce_IdentityDb;
GO

-- 2. Tạo bảng Roles (Vai trò)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE roles (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(20) NOT NULL UNIQUE
    );
END;

-- 3. Tạo bảng Users (Người dùng)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        full_name NVARCHAR(100) NULL,
        is_active BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE()
    );
END;

-- 4. Tạo bảng trung gian user_roles
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[user_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE user_roles (
        user_id BIGINT NOT NULL,
        role_id INT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        CONSTRAINT FK_UserRoles_Users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT FK_UserRoles_Roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );
END;

-- 5. Chèn dữ liệu mẫu nếu bảng chưa có dữ liệu
IF NOT EXISTS (SELECT * FROM roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER');

IF NOT EXISTS (SELECT * FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
GO