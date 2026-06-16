package com.inventory.api.mapper;

import com.inventory.api.dto.ItemResponse;
import com.inventory.api.models.Item;
import com.inventory.api.mapper.PhotoMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PhotoMapper.class})
public interface ItemMapper {
    ItemResponse toResponse(Item item);
}