package com.uniqueverse.favorite.service;

import com.uniqueverse.auth.entity.User;
import com.uniqueverse.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            PlanetRepository planetRepository,
            UserRepository userRepository) {

        this.favoriteRepository = favoriteRepository;
        this.planetRepository = planetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FavoriteResponse createFavorite(
            Long planetId,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Planet planet = planetRepository.findById(planetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Planet not found with id: " + planetId));

        Favorite favorite = new Favorite(user, planet);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return new FavoriteResponse(
                savedFavorite.getId(),
                planet.getId(),
                planet.getName()
        );
    }

    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteById(
            Long id,
            String username) {

        Favorite favorite =
                favoriteRepository
                        .findByIdWithPlanetAndUsername(id, username)
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
    public List<FavoriteResponse> getAllFavorites(
            String username) {

        return favoriteRepository
                .findAllWithPlanetByUsername(username)
                .stream()
                .map(favorite -> new FavoriteResponse(
                        favorite.getId(),
                        favorite.getPlanet().getId(),
                        favorite.getPlanet().getName()
                ))
                .toList();
    }
}