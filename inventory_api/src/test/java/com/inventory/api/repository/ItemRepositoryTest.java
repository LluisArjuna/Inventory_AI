package com.inventory.api.repository;

import com.inventory.api.models.Item;
import com.inventory.api.models.Inventory;
import com.inventory.api.models.Category;
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
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Inventory createInventory() {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        User savedUser = userRepository.save(user);

        return inventoryRepository.save(Inventory.builder()
                .isPublic(true)
                .user(savedUser)
                .build());
    }

    private Category createCategory() {
        return categoryRepository.save(Category.builder()
                .name("Antiques")
                .description("Antique items")
                .build());
    }

    @Test
    void save_shouldPersistItem() {
        Inventory inventory = createInventory();
        Category category = createCategory();

        Item item = Item.builder()
                .name("Ancient Vase")
                .description("Roman artifact")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .build();

        Item saved = itemRepository.save(item);

        assertNotNull(saved.getId());
        assertEquals("Ancient Vase", saved.getName());
        assertEquals(1850, saved.getYear());
    }

    @Test
    void findByInventoryId_shouldReturnItems() {
        Inventory inventory = createInventory();
        Category category = createCategory();

        Item item1 = Item.builder()
                .name("Item 1")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .build();
        Item item2 = Item.builder()
                .name("Item 2")
                .year(1860)
                .inventory(inventory)
                .category(category)
                .build();

        itemRepository.save(item1);
        itemRepository.save(item2);

        Page<Item> result = itemRepository.findByInventoryId(inventory.getId(), Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByCategoryId_shouldReturnItems() {
        Inventory inventory = createInventory();
        Category category = createCategory();

        Item item = Item.builder()
                .name("Ancient Vase")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .build();

        itemRepository.save(item);

        Page<Item> result = itemRepository.findByCategoryId(category.getId(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Ancient Vase", result.getContent().get(0).getName());
    }

    @Test
    void findByYear_shouldReturnItems() {
        Inventory inventory = createInventory();
        Category category = createCategory();

        Item item = Item.builder()
                .name("Ancient Vase")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .build();

        itemRepository.save(item);

        Page<Item> result = itemRepository.findByYear(1850, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void delete_shouldRemoveItem() {
        Inventory inventory = createInventory();
        Category category = createCategory();

        Item item = Item.builder()
                .name("Ancient Vase")
                .year(1850)
                .inventory(inventory)
                .category(category)
                .build();

        Item saved = itemRepository.save(item);
        itemRepository.deleteById(saved.getId());

        assertFalse(itemRepository.existsById(saved.getId()));
    }
}