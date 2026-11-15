package com.uniqueverse.favorite.service;

import com.uniqueverse.exception.ResourceNotFoundException;
import com.uniqueverse.favorite.dto.FavoriteResponse;
import com.uniqueverse.favorite.entity.Favorite;
import com.uniqueverse.favorite.repository.FavoriteRepository;
import com.uniqueverse.planet.entity.Planet;
import com.uniqueverse.planet.repository.PlanetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PlanetRepository planetRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            PlanetRepository planetRepository) {

        this.favoriteRepository = favoriteRepository;
        this.planetRepository = planetRepository;
    }

    @Transactional
    public FavoriteResponse createFavorite(Long planetId) {

        Planet planet = planetRepository.findById(planetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Planet not found with id: " + planetId));

        Favorite favorite = new Favorite(planet);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return new FavoriteResponse(
                savedFavorite.getId(),
                planet.getId(),
                planet.getName()
        );
    }


    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteById(Long id) {

        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Favorite not found with id: " + id));

        return new FavoriteResponse(
                favorite.getId(),
                favorite.getPlanet().getId(),
                favorite.getPlanet().getName()
        );
    }


    @Transactional(readOnly = true)
    public List<FavoriteResponse> getAllFavorites() {

        return favoriteRepository.findAllWithPlanet()
                .stream()
                .map(favorite -> new FavoriteResponse(
                        favorite.getId(),
                        favorite.getPlanet().getId(),
                        favorite.getPlanet().getName()
                ))
                .toList();
    }
}