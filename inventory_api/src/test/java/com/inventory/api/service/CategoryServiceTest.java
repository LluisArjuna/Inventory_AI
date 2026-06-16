package com.inventory.api.service;

import com.inventory.api.dto.CategoryResponse;
import com.inventory.api.dto.CreateCategoryRequest;
import com.inventory.api.dto.UpdateCategoryRequest;
import com.inventory.api.models.Category;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.CategoryMapper;
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
class CategoryServiceTest {

    @Mock
    private com.inventory.api.repository.CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponse categoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = Category.builder()
                .id(categoryId)
                .name("Antiques")
                .description("Antique items")
                .build();
        categoryResponse = new CategoryResponse(categoryId, "Antiques", "Antique items");
    }

    @Test
    void create_shouldReturnCategoryResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest("Antiques", "Antique items");
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals("Antiques", result.name());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void findById_shouldReturnCategoryResponse() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.findById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.id());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(categoryId));
    }

    @Test
    void update_shouldReturnUpdatedCategoryResponse() {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated", "Updated description");
        Category updatedCategory = Category.builder()
                .id(categoryId)
                .name("Updated")
                .description("Updated description")
                .build();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(
                new CategoryResponse(categoryId, "Updated", "Updated description"));

        CategoryResponse result = categoryService.update(categoryId, request);

        assertNotNull(result);
        assertEquals("Updated", result.name());
    }

    @Test
    void delete_shouldCallRepository() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(categoryId);

        categoryService.delete(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(categoryId));
    }
}