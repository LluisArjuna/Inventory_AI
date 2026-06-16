package com.inventory.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.api.dto.CategoryResponse;
import com.inventory.api.dto.CreateCategoryRequest;
import com.inventory.api.dto.UpdateCategoryRequest;
import com.inventory.api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void create_shouldReturnCreated() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest("Antiques", "Antique items");
        CategoryResponse response = new CategoryResponse(categoryId, "Antiques", "Antique items");

        when(categoryService.create(any(CreateCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Antiques"));
    }

    @Test
    @WithMockUser
    void getById_shouldReturnCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CategoryResponse response = new CategoryResponse(categoryId, "Antiques", "Antique items");

        when(categoryService.findById(categoryId)).thenReturn(response);

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Antiques"));
    }

    @Test
    @WithMockUser
    void getAll_shouldReturnPage() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CategoryResponse response = new CategoryResponse(categoryId, "Antiques", "Antique items");
        Page<CategoryResponse> page = new PageImpl<>(Collections.singletonList(response));

        when(categoryService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Antiques"));
    }

    @Test
    @WithMockUser
    void update_shouldReturnUpdatedCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated", "Updated description");
        CategoryResponse response = new CategoryResponse(categoryId, "Updated", "Updated description");

        when(categoryService.update(eq(categoryId), any(UpdateCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser
    void delete_shouldReturnNoContent() throws Exception {
        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_withoutAuth_shouldReturnUnauthorized() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Antiques", "Antique items");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}