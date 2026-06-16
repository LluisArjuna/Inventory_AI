package com.inventory.api.mapper;

import com.inventory.api.dto.PhotoResponse;
import com.inventory.api.models.Photo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoResponse toResponse(Photo photo);
}