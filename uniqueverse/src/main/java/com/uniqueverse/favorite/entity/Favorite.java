package com.uniqueverse.favorite.entity;

import com.uniqueverse.planet.entity.Planet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "favorites")
public class Favorite {

    public Favorite() {
    }

    public Favorite(Planet planet) {
        this.planet = planet;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    private Planet planet;

}
