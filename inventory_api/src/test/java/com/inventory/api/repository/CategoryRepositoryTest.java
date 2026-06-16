package com.inventory.api.repository;

import com.inventory.api.models.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void save_shouldPersistCategory() {
        Category category = Category.builder()
                .name("Antiques")
                .description("Antique items")
                .build();

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
        assertEquals("Antiques", saved.getName());
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatches() {
        Category category1 = Category.builder().name("Antiques").build();
        Category category2 = Category.builder().name("antique furniture").build();
        Category category3 = Category.builder().name("Electronics").build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        Page<Category> result = categoryRepository.findByNameContainingIgnoreCase("antique", Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findById_shouldReturnCategory() {
        Category category = Category.builder()
                .name("Antiques")
                .description("Antique items")
                .build();

        Category saved = categoryRepository.save(category);

        Category found = categoryRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Antiques", found.getName());
    }

    @Test
    void delete_shouldRemoveCategory() {
        Category category = Category.builder()
                .name("Antiques")
                .build();

        Category saved = categoryRepository.save(category);
        categoryRepository.deleteById(saved.getId());

        assertFalse(categoryRepository.existsById(saved.getId()));
    }
}