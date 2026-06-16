package com.inventory.api.service;

import com.inventory.api.dto.InventoryResponse;
import com.inventory.api.dto.CreateInventoryRequest;
import com.inventory.api.dto.UpdateInventoryRequest;
import com.inventory.api.models.Inventory;
import com.inventory.api.models.User;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.InventoryMapper;
import com.inventory.api.repository.InventoryRepository;
import com.inventory.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;
    private InventoryResponse inventoryResponse;
    private UUID inventoryId;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        inventoryId = UUID.randomUUID();
        userId = UUID.randomUUID();
        user = User.builder().id(userId).email("test@example.com").build();
        inventory = Inventory.builder()
                .id(inventoryId)
                .name("Test Inventory")
                .description("A test inventory")
                .isPublic(true)
                .user(user)
                .build();
        inventoryResponse = new InventoryResponse(inventoryId, "Test Inventory", "A test inventory", true, userId, "test@example.com", null);
    }

    @Test
    void create_shouldReturnInventoryResponse() {
        CreateInventoryRequest request = new CreateInventoryRequest("Test Inventory", "A test inventory", true, userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.create(request);

        assertNotNull(result);
        assertTrue(result.isPublic());
        assertEquals("Test Inventory", result.name());
        assertEquals("A test inventory", result.description());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenUserNotFound() {
        CreateInventoryRequest request = new CreateInventoryRequest("Test Inventory", "A test inventory", true, userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.create(request));
    }

    @Test
    void findById_shouldReturnInventoryResponse() {
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryMapper.toResponse(inventory)).thenReturn(inventoryResponse);

        InventoryResponse result = inventoryService.findById(inventoryId);

        assertNotNull(result);
        assertEquals(inventoryId, result.id());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException() {
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.findById(inventoryId));
    }

    @Test
    void update_shouldReturnUpdatedInventoryResponse() {
        UpdateInventoryRequest request = new UpdateInventoryRequest("Updated Inventory", "Updated description", false);
        Inventory updatedInventory = Inventory.builder()
                .id(inventoryId)
                .name("Updated Inventory")
                .description("Updated description")
                .isPublic(false)
                .user(user)
                .build();
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);
        when(inventoryMapper.toResponse(updatedInventory)).thenReturn(new InventoryResponse(inventoryId, "Updated Inventory", "Updated description", false, userId, "test@example.com", null));

        InventoryResponse result = inventoryService.update(inventoryId, request);

        assertNotNull(result);
        assertFalse(result.isPublic());
        assertEquals("Updated Inventory", result.name());
        assertEquals("Updated description", result.description());
    }

    @Test
    void delete_shouldCallRepository() {
        when(inventoryRepository.existsById(inventoryId)).thenReturn(true);
        doNothing().when(inventoryRepository).deleteById(inventoryId);

        inventoryService.delete(inventoryId);

        verify(inventoryRepository).deleteById(inventoryId);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(inventoryRepository.existsById(inventoryId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.delete(inventoryId));
    }
}