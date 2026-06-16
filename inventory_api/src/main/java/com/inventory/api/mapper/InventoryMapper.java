package com.inventory.api.mapper;

import com.inventory.api.dto.InventoryResponse;
import com.inventory.api.models.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(target = "userName", expression = "java(inventory.getUser().getEmail())")
    InventoryResponse toResponse(Inventory inventory);
}