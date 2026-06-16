package com.inventory.api.service;

import com.inventory.api.dto.ItemResponse;
import com.inventory.api.dto.CreateItemRequest;
import com.inventory.api.dto.UpdateItemRequest;
import com.inventory.api.models.*;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.ItemMapper;
import com.inventory.api.mapper.PhotoMapper;
import com.inventory.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CoordinateRepository coordinateRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private PhotoMapper photoMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemResponse itemResponse;
    private UUID itemId;
    private UUID inventoryId;
    private UUID categoryId;
    private Inventory inventory;
    private Category category;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        inventoryId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        inventory = Inventory.builder().id(inventoryId).isPublic(true).user(user).build();
        category = Category.builder().id(categoryId).name("Antiques").build();

        item = Item.builder()
                .id(itemId)
                .name("Ancient Vase")
                .description("Roman artifact")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .photos(new ArrayList<>())
                .build();

        itemResponse = new ItemResponse(
                itemId, "Ancient Vase", "Roman artifact", 1850,
                inventoryId, categoryId, null, new ArrayList<>());
    }

    @Test
    void create_shouldReturnItemResponse() {
        CreateItemRequest request = new CreateItemRequest(
                "Ancient Vase", "Roman artifact", 1850, inventoryId, categoryId, null);
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);
        when(photoMapper.toResponse(any(Photo.class))).thenReturn(null);

        ItemResponse result = itemService.create(request);

        assertNotNull(result);
        assertEquals("Ancient Vase", result.name());
        assertEquals(1850, result.year());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenInventoryNotFound() {
        CreateItemRequest request = new CreateItemRequest(
                "Ancient Vase", "Roman artifact", 1850, inventoryId, categoryId, null);
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.create(request));
    }

    @Test
    void findById_shouldReturnItemResponse() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.findById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.id());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.findById(itemId));
    }

    @Test
    void update_shouldReturnUpdatedItemResponse() {
        UpdateItemRequest request = new UpdateItemRequest("Updated Vase", null, 1900, null, null, null);
        Item updatedItem = Item.builder()
                .id(itemId)
                .name("Updated Vase")
                .description("Roman artifact")
                .year(1900)
                .inventory(inventory)
                .category(category)
                .photos(new ArrayList<>())
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toResponse(updatedItem)).thenReturn(
                new ItemResponse(itemId, "Updated Vase", "Roman artifact", 1900,
                        inventoryId, categoryId, null, new ArrayList<>()));

        ItemResponse result = itemService.update(itemId, request);

        assertNotNull(result);
        assertEquals("Updated Vase", result.name());
        assertEquals(1900, result.year());
    }

    @Test
    void delete_shouldCallRepository() {
        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(itemId);

        itemService.delete(itemId);

        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> itemService.delete(itemId));
    }
}