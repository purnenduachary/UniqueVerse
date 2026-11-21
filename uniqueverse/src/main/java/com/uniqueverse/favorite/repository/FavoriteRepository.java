package com.uniqueverse.favorite.repository;

import com.uniqueverse.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("""
        SELECT f
        FROM Favorite f
        JOIN FETCH f.planet
        WHERE f.user.username = :username
        """)
    List<Favorite> findAllWithPlanetByUsername(
            @Param("username") String username
    );

    @Query("""
        SELECT f
        FROM Favorite f
        JOIN FETCH f.planet
        WHERE f.id = :id
        AND f.user.username = :username
        """)
    Optional<Favorite> findByIdWithPlanetAndUsername(
            @Param("id") Long id,
            @Param("username") String username
    );

    @Query("""
    SELECT f
    FROM Favorite f
    WHERE f.id = :id
    AND f.user.username = :username
    """)
    Optional<Favorite> findByIdAndUsername(
            @Param("id") Long id,
            @Param("username") String username
    );

    boolean existsByUserUsernameAndPlanetId(
            String username,
            Long planetId
    );
}