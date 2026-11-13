package com.uniqueverse.planet.controller;

import com.uniqueverse.planet.entity.Planet;
import com.uniqueverse.planet.service.PlanetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlanetController {

    private final PlanetService planetService;

    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }

    @GetMapping("/api/v1/planets")
    public List<Planet> getAllPlanets() {
        return planetService.getAllPlanets();
    }

    @GetMapping("/api/v1/planets/{id}")
    public Planet getPlanetById(@PathVariable Long id) {
        return planetService.getPlanetById(id);
    }
}