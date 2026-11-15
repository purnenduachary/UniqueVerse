package com.uniqueverse.planet.controller;

import com.uniqueverse.common.response.PageResponse;
import com.uniqueverse.planet.entity.Planet;
import com.uniqueverse.planet.service.PlanetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
public class PlanetController {

    private final PlanetService planetService;

    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }

    @GetMapping("/api/v1/planets")
    public PageResponse<Planet> getAllPlanets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            Pageable pageable) {

        Page<Planet> planets =
                planetService.getAllPlanets(query, type, pageable);

        return new PageResponse<>(planets);
    }

    @GetMapping("/api/v1/planets/{id}")
    public Planet getPlanetById(@PathVariable Long id) {
        return planetService.getPlanetById(id);
    }


    @PostMapping("/api/v1/planets")
    public ResponseEntity<Planet> createPlanet(@RequestBody Planet planet) {
        Planet savedPlanet = planetService.createPlanet(planet);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlanet);
    }



    @PutMapping("/api/v1/planets/{id}")
    public ResponseEntity<Planet> updatePlanet(
            @PathVariable Long id,
            @RequestBody Planet planet) {

        Planet updatedPlanet = planetService.updatePlanet(id, planet);

        return ResponseEntity.ok(updatedPlanet);
    }


    @DeleteMapping("/api/v1/planets/{id}")
    public ResponseEntity<Void> deletePlanet(@PathVariable Long id) {
        planetService.deletePlanet(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/api/v1/planets/name/{name}")
    public Planet getPlanetByName(@PathVariable String name) {
        return planetService.getPlanetByName(name);
    }


}