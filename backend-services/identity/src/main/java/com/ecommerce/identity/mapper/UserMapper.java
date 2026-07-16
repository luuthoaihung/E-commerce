package com.ecommerce.identity.mapper;

import com.ecommerce.identity.dto.request.ProfileRequest;
import com.ecommerce.identity.dto.response.ProfileResponse;
import com.ecommerce.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @SuppressWarnings("unused") // Thêm dòng này để tắt cảnh báo
    ProfileResponse toProfileResponse(User user);

    @SuppressWarnings("unused") // Thêm dòng này để tắt cảnh báo
    void updateUser(@MappingTarget User user, ProfileRequest request);
}