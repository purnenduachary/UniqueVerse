package com.uniqueverse.planet.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "planets")
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "distance_from_sun_km")
    private Double distanceFromSunKm;

    @Column(name = "radius_km")
    private Double radiusKm;

    @Column(name = "mass_kg")
    private Double massKg;

    @Column(name = "orbital_period_days")
    private Double orbitalPeriodDays;

    @Column(name = "rotation_period_hours")
    private Double rotationPeriodHours;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    // Getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDistanceFromSunKm() {
        return distanceFromSunKm;
    }

    public void setDistanceFromSunKm(Double distanceFromSunKm) {
        this.distanceFromSunKm = distanceFromSunKm;
    }

    public Double getMassKg() {
        return massKg;
    }

    public void setMassKg(Double massKg) {
        this.massKg = massKg;
    }

    public Double getOrbitalPeriodDays() {
        return orbitalPeriodDays;
    }

    public void setOrbitalPeriodDays(Double orbitalPeriodDays) {
        this.orbitalPeriodDays = orbitalPeriodDays;
    }

    public Double getRotationPeriodHours() {
        return rotationPeriodHours;
    }

    public void setRotationPeriodHours(Double rotationPeriodHours) {
        this.rotationPeriodHours = rotationPeriodHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}