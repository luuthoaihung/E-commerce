package com.ecommerce.identity.mapper;

import com.ecommerce.identity.dto.response.ProductResponse;
import com.ecommerce.identity.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class}) // 🌟 Thêm dòng này để MapStruct gọi CategoryMapper
public interface ProductMapper {
    ProductResponse toProductResponse(Product product);
}