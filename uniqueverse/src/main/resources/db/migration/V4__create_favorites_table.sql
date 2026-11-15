CREATE TABLE favorites (
                           id BIGSERIAL PRIMARY KEY,
                           planet_id BIGINT NOT NULL,
                           CONSTRAINT fk_favorites_planet
                               FOREIGN KEY (planet_id)
                                   REFERENCES planets(id)
                                   ON DELETE CASCADE
);