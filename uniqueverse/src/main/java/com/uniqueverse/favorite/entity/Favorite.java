package com.uniqueverse.favorite.entity;

import com.uniqueverse.auth.entity.User;
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

    public Favorite(User user, Planet planet) {
        this.user = user;
        this.planet = planet;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    private Planet planet;
}