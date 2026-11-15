package com.uniqueverse.favorite.dto;

public class FavoriteResponse {

    private Long id;
    private Long planetId;
    private String planetName;

    public FavoriteResponse(Long id, Long planetId, String planetName) {
        this.id = id;
        this.planetId = planetId;
        this.planetName = planetName;
    }

    public Long getId() {
        return id;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public String getPlanetName() {
        return planetName;
    }
}