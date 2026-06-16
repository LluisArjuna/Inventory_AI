package com.inventory.api.repository;

import com.inventory.api.models.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, UUID> {
}