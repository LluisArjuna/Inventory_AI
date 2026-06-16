package com.inventory.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
@Tag(name = "Geocode", description = "Geocoding proxy to Nominatim")
public class GeocodeController {

    private final RestTemplate restTemplate;

    @GetMapping("/reverse")
    @Operation(summary = "Reverse geocode lat/lon to address via Nominatim")
    public String reverse(@RequestParam double lat, @RequestParam double lon) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("format", "json")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, "InventoryApp/1.0");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a country bounding box via Nominatim")
    public String search(@RequestParam String q) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", q)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("featuretype", "country")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, "InventoryApp/1.0");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
}
