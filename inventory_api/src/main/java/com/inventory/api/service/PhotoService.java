package com.inventory.api.service;

import com.inventory.api.dto.*;
import com.inventory.api.models.Item;
import com.inventory.api.models.Photo;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.PhotoMapper;
import com.inventory.api.repository.ItemRepository;
import com.inventory.api.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final ItemRepository itemRepository;
    private final PhotoMapper photoMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public PhotoResponse upload(MultipartFile file, UUID itemId, Integer position, String altText, String format) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        Map<String, Object> uploadResult = cloudinaryService.upload(file, format);
        String url = (String) uploadResult.get("url");

        Photo photo = Photo.builder()
                .item(item)
                .url(url)
                .position(position)
                .altText(altText)
                .build();

        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @Transactional
    public PhotoResponse create(CreatePhotoRequest request) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", request.itemId()));

        Photo photo = Photo.builder()
                .item(item)
                .url(request.url())
                .position(request.position())
                .altText(request.altText())
                .build();

        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @Transactional(readOnly = true)
    public PhotoResponse findById(UUID id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", id));
        return photoMapper.toResponse(photo);
    }

    @Transactional(readOnly = true)
    public Page<PhotoResponse> findAll(Pageable pageable) {
        return photoRepository.findAll(pageable).map(photoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PhotoResponse> findByItemId(UUID itemId, Pageable pageable) {
        return photoRepository.findByItemId(itemId, pageable).map(photoMapper::toResponse);
    }

    @Transactional
    public PhotoResponse update(UUID id, UpdatePhotoRequest request) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", id));

        if (request.url() != null) {
            photo.setUrl(request.url());
        }
        if (request.position() != null) {
            photo.setPosition(request.position());
        }
        if (request.altText() != null) {
            photo.setAltText(request.altText());
        }

        photo = photoRepository.save(photo);
        return photoMapper.toResponse(photo);
    }

    @Transactional
    public void delete(UUID id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", id));

        String publicId = extractPublicId(photo.getUrl());
        if (publicId != null) {
            cloudinaryService.delete(publicId);
        }

        photoRepository.delete(photo);
    }

    private String extractPublicId(String url) {
        if (url == null) return null;
        try {
            String[] parts = url.split("/");
            String last = parts[parts.length - 1];
            return last.contains(".") ? last.substring(0, last.lastIndexOf('.')) : last;
        } catch (Exception e) {
            return null;
        }
    }
}