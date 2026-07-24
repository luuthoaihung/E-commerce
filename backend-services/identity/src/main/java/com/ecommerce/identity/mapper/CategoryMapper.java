package com.ecommerce.identity.mapper;

import com.ecommerce.identity.dto.response.CategoryResponse;
import com.ecommerce.identity.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}