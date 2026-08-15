package com.ecommerce.identity.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.identity.dto.request.ProductRequest;
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.dto.response.ProductResponse;
import com.ecommerce.identity.entity.Product; // Import entity
import com.ecommerce.identity.mapper.ProductMapper; // Import mapper
import com.ecommerce.identity.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
 

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request)) // Gọi trực tiếp vì service đã trả về ProductResponse
                .build();
    }

    @GetMapping("/list")
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        BigDecimal min = (minPrice != null && !minPrice.trim().isEmpty()) ? new BigDecimal(minPrice) : null;
        BigDecimal max = (maxPrice != null && !maxPrice.trim().isEmpty()) ? new BigDecimal(maxPrice) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductResponse> productPage = productService.getProductsWithFilter(name, categoryId, min, max, pageable);

        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productPage)
                .build();
    }

    @GetMapping("/info/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable String id) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(id)) // Gọi trực tiếp
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/edit/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable String id, @RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(id, request)) // Gọi trực tiếp
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .result("Product has been deleted successfully")
                .build();
    }
}