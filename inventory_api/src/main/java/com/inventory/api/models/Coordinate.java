package com.inventory.api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "coordinates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @Column(name = "coord_x", nullable = false, precision = 10, scale = 6)
    private BigDecimal coordX;

    @Column(name = "coord_y", nullable = false, precision = 10, scale = 6)
    private BigDecimal coordY;

    @OneToMany(mappedBy = "coordinate")
    @Builder.Default
    private List<Item> items = new ArrayList<>();
}