package com.inventory.api.mapper;

import com.inventory.api.dto.UserResponse;
import com.inventory.api.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}