package com.uniqueverse.favorite.repository;

import com.uniqueverse.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("""
        SELECT f
        FROM Favorite f
        JOIN FETCH f.planet
        """)
    List<Favorite> findAllWithPlanet();
}