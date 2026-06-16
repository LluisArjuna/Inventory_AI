package com.inventory.api.mapper;

import com.inventory.api.dto.CoordinateResponse;
import com.inventory.api.models.Coordinate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoordinateMapper {
    CoordinateResponse toResponse(Coordinate coordinate);
}