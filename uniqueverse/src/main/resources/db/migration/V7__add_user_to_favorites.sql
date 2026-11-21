ALTER TABLE favorites
    ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE favorites
    ADD CONSTRAINT uk_favorites_user_planet
        UNIQUE (user_id, planet_id);

CREATE INDEX idx_favorites_user_id
    ON favorites(user_id);