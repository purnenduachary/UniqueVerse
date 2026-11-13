CREATE TABLE planets (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         description TEXT,
                         distance_from_sun_km DOUBLE PRECISION,
                         radius_km DOUBLE PRECISION,
                         mass_kg DOUBLE PRECISION,
                         orbital_period_days DOUBLE PRECISION,
                         rotation_period_hours DOUBLE PRECISION,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);