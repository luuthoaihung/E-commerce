package com.ecommerce.identity.service;

import java.util.List;

import com.ecommerce.identity.entity.Category;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.dto.request.CategoryRequest; // Hoặc đường dẫn package chứa CategoryRequest của anh
import com.ecommerce.identity.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED); // Nhớ thêm mã lỗi này trong ErrorCode nhé
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Kiểm tra nếu đổi tên trùng với tên danh mục khác đã tồn tại
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }
}
