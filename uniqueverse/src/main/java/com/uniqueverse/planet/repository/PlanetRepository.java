package com.uniqueverse.planet.repository;

import com.uniqueverse.planet.entity.Planet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlanetRepository extends JpaRepository<Planet, Long> {

    Optional<Planet> findByName(String name);

    @Query("""
        SELECT p FROM Planet p
        WHERE (:query IS NULL OR :query = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:type IS NULL OR :type = ''
             OR LOWER(p.type) = LOWER(:type))
        """)
    Page<Planet> findByFilters(
            @Param("query") String query,
            @Param("type") String type,
            Pageable pageable
    );
}