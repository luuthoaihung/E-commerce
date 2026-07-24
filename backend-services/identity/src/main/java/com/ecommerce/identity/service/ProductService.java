package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.ProductRequest;
import com.ecommerce.identity.dto.response.ProductResponse;
import com.ecommerce.identity.entity.Category;
import com.ecommerce.identity.entity.Product;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.mapper.ProductMapper;
import com.ecommerce.identity.repository.CategoryRepository;
import com.ecommerce.identity.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    // 🌟 Thêm hàm Tạo mới sản phẩm
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public Page<ProductResponse> getProductsWithFilter(String name, String categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Lọc theo tên sản phẩm (gần đúng - chứa ký tự)
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        // Lọc theo Category ID
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        // Lọc theo giá từ (minPrice)
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        // Lọc theo giá đến (maxPrice)
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(productMapper::toProductResponse);
    }

    // 🌟 Thêm hàm Lấy chi tiết sản phẩm theo ID
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        // Tìm sản phẩm cần cập nhật
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Tìm category mới
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Cập nhật thông tin
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }
    
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }
}