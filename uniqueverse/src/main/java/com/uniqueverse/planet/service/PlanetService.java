package com.uniqueverse.planet.service;

import com.uniqueverse.exception.ResourceNotFoundException;
import com.uniqueverse.planet.entity.Planet;
import com.uniqueverse.planet.repository.PlanetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanetService {

    private final PlanetRepository planetRepository;

    public PlanetService(PlanetRepository planetRepository) {
        this.planetRepository = planetRepository;
    }

    public List<Planet> getAllPlanets() {
        return planetRepository.findAll();
    }

    public Planet getPlanetById(Long id) {
        return planetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planet not found with id: " + id));
    }
}