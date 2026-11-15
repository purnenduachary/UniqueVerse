package com.uniqueverse.planet.service;

import com.uniqueverse.exception.ResourceNotFoundException;
import com.uniqueverse.planet.entity.Planet;
import com.uniqueverse.planet.repository.PlanetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanetService {

    private final PlanetRepository planetRepository;

    public PlanetService(PlanetRepository planetRepository) {
        this.planetRepository = planetRepository;
    }

    public Page<Planet> getAllPlanets(
            String query,
            String type,
            Pageable pageable) {

        return planetRepository.findByFilters(query, type, pageable);
    }

    public Planet getPlanetById(Long id) {
        return planetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planet not found with id: " + id));
    }



    @Transactional
    public Planet createPlanet(Planet planet) {
        return planetRepository.save(planet);
    }


    @Transactional
    public Planet updatePlanet(Long id, Planet updatedPlanet) {

        Planet existingPlanet = planetRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Planet not found with id: " + id));

        existingPlanet.setName(updatedPlanet.getName());
        existingPlanet.setDescription(updatedPlanet.getDescription());
        existingPlanet.setDistanceFromSunKm(updatedPlanet.getDistanceFromSunKm());
        existingPlanet.setRadiusKm(updatedPlanet.getRadiusKm());
        existingPlanet.setMassKg(updatedPlanet.getMassKg());
        existingPlanet.setOrbitalPeriodDays(updatedPlanet.getOrbitalPeriodDays());
        existingPlanet.setRotationPeriodHours(updatedPlanet.getRotationPeriodHours());
        existingPlanet.setType(updatedPlanet.getType());
        existingPlanet.setInterestingFact(updatedPlanet.getInterestingFact());

        return planetRepository.save(existingPlanet);

    }


    @Transactional
    public void deletePlanet(Long id) {

        Planet planet = planetRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Planet not found with id: " + id));

        planetRepository.delete(planet);
    }



    public Planet getPlanetByName(String name) {
        return planetRepository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Planet not found with name: " + name));
    }





}