package com.inventory.api.service;

import com.inventory.api.dto.*;
import com.inventory.api.models.Coordinate;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.CoordinateMapper;
import com.inventory.api.repository.CoordinateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoordinateService {

    private final CoordinateRepository coordinateRepository;
    private final CoordinateMapper coordinateMapper;

    @Transactional
    public CoordinateResponse create(CreateCoordinateRequest request) {
        Coordinate coordinate = Coordinate.builder()
                .coordX(request.coordX())
                .coordY(request.coordY())
                .build();

        coordinate = coordinateRepository.save(coordinate);
        return coordinateMapper.toResponse(coordinate);
    }

    @Transactional(readOnly = true)
    public CoordinateResponse findById(UUID id) {
        Coordinate coordinate = coordinateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinate", "id", id));
        return coordinateMapper.toResponse(coordinate);
    }

    @Transactional(readOnly = true)
    public Page<CoordinateResponse> findAll(Pageable pageable) {
        return coordinateRepository.findAll(pageable).map(coordinateMapper::toResponse);
    }

    @Transactional
    public CoordinateResponse update(UUID id, UpdateCoordinateRequest request) {
        Coordinate coordinate = coordinateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinate", "id", id));

        coordinate.setCoordX(request.coordX());
        coordinate.setCoordY(request.coordY());

        coordinate = coordinateRepository.save(coordinate);
        return coordinateMapper.toResponse(coordinate);
    }

    @Transactional
    public void delete(UUID id) {
        if (!coordinateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coordinate", "id", id);
        }
        coordinateRepository.deleteById(id);
    }
}