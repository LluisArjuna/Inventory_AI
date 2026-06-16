package com.inventory.api.controller;

import com.inventory.api.dto.*;
import com.inventory.api.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Tag(name = "Photos", description = "Photo management endpoints")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload photo to Cloudinary and create photo record")
    public ResponseEntity<PhotoResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("itemId") UUID itemId,
            @RequestParam("position") Integer position,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam("format") String format) {
        return ResponseEntity.status(HttpStatus.CREATED).body(photoService.upload(file, itemId, position, altText, format));
    }


    @PostMapping
    @Operation(summary = "Create new photo with existing URL")
    public ResponseEntity<PhotoResponse> create(@Valid @RequestBody CreatePhotoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(photoService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get photo by ID")
    public ResponseEntity<PhotoResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(photoService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all photos with pagination")
    public ResponseEntity<Page<PhotoResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(photoService.findAll(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update photo")
    public ResponseEntity<PhotoResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePhotoRequest request) {
        return ResponseEntity.ok(photoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete photo")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        photoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}