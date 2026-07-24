package com.ecommerce.identity.repository;

import java.math.BigDecimal;

import com.ecommerce.identity.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    // Lọc theo tên sản phẩm (tìm gần đúng - chứa từ khóa)
    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) -> 
            (name == null || name.trim().isEmpty()) ? null : 
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    // Lọc theo danh mục
    public static Specification<Product> hasCategory(String categoryId) {
        return (root, query, criteriaBuilder) -> 
            (categoryId == null || categoryId.trim().isEmpty()) ? null : 
            criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    // Lọc giá lớn hơn hoặc bằng minPrice
    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> 
            minPrice == null ? null : 
            criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    // Lọc giá nhỏ hơn hoặc bằng maxPrice
    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> 
            maxPrice == null ? null : 
            criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}