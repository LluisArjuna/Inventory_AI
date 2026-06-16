package com.inventory.api.repository;

import com.inventory.api.models.Inventory;
import com.inventory.api.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private com.inventory.api.repository.UserRepository userRepository;

    @Test
    void save_shouldPersistInventory() {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        User savedUser = userRepository.save(user);

        Inventory inventory = Inventory.builder()
                .name("Test Inventory")
                .isPublic(true)
                .user(savedUser)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        assertNotNull(saved.getId());
        assertTrue(saved.getIsPublic());
        assertEquals(savedUser.getId(), saved.getUser().getId());
        assertEquals("Test Inventory", saved.getName());
    }

    @Test
    void findByUserId_shouldReturnUserInventories() {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        User savedUser = userRepository.save(user);

        Inventory inventory1 = Inventory.builder().name("Inventory 1").isPublic(true).user(savedUser).build();
        Inventory inventory2 = Inventory.builder().name("Inventory 2").isPublic(false).user(savedUser).build();

        inventoryRepository.save(inventory1);
        inventoryRepository.save(inventory2);

        Page<Inventory> result = inventoryRepository.findByUserId(savedUser.getId(), Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findById_shouldReturnInventory() {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        User savedUser = userRepository.save(user);

        Inventory inventory = Inventory.builder()
                .name("Test Inventory")
                .isPublic(true)
                .user(savedUser)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        Inventory found = inventoryRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertTrue(found.getIsPublic());
        assertEquals("Test Inventory", found.getName());
    }

    @Test
    void delete_shouldRemoveInventory() {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        User savedUser = userRepository.save(user);

        Inventory inventory = Inventory.builder()
                .name("Test Inventory")
                .isPublic(true)
                .user(savedUser)
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        inventoryRepository.deleteById(saved.getId());

        assertFalse(inventoryRepository.existsById(saved.getId()));
    }
}