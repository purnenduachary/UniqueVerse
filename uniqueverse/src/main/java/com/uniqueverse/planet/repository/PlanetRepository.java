package com.uniqueverse.planet.repository;

import com.uniqueverse.planet.entity.Planet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanetRepository extends JpaRepository<Planet, Long> {

}