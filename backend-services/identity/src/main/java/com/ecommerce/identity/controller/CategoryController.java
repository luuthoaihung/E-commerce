package com.ecommerce.identity.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.identity.dto.request.CategoryRequest;
import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.entity.Category;
import com.ecommerce.identity.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')") // 🌟 Chỉ Admin mới được tạo
    @PostMapping("/add")
    public ApiResponse<Category> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<Category>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @GetMapping("/list") // Ai cũng xem được (User và Admin)
    public ApiResponse<List<Category>> getAllCategories() {
        return ApiResponse.<List<Category>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')") // 🌟 Chỉ Admin mới được sửa
    @PutMapping("edit/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable String id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<Category>builder()
                .result(categoryService.updateCategory(id, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')") // 🌟 Chỉ Admin mới được xóa
    @DeleteMapping("delete/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .result("Category has been deleted successfully")
                .build();
    }
}
