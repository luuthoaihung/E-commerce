package com.ecommerce.identity.mapper;

import com.ecommerce.identity.dto.request.ProfileRequest;
import com.ecommerce.identity.dto.response.ProfileResponse;
import com.ecommerce.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ProfileResponse toProfileResponse(User user);
    void updateUser(@MappingTarget User user, ProfileRequest request);
}