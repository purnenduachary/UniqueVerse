package com.uniqueverse.favorite.controller;

import com.uniqueverse.favorite.dto.FavoriteResponse;
import com.uniqueverse.favorite.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponse createFavorite(
            @RequestParam Long planetId,
            Authentication authentication) {

        return favoriteService.createFavorite(
                planetId,
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public FavoriteResponse getFavoriteById(
            @PathVariable Long id,
            Authentication authentication) {

        return favoriteService.getFavoriteById(
                id,
                authentication.getName()
        );
    }

    @GetMapping
    public List<FavoriteResponse> getAllFavorites(
            Authentication authentication) {

        return favoriteService.getAllFavorites(
                authentication.getName()
        );
    }
}