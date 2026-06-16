package com.inventory.api.mapper;

import com.inventory.api.dto.CategoryResponse;
import com.inventory.api.models.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}