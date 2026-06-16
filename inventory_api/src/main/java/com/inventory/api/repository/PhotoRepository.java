package com.inventory.api.repository;

import com.inventory.api.models.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByItemIdOrderByPositionAsc(UUID itemId);
    Page<Photo> findByItemId(UUID itemId, Pageable pageable);
}