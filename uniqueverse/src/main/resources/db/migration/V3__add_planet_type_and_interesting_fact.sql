ALTER TABLE planets
    ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'unknown';

ALTER TABLE planets
    ADD COLUMN interesting_fact TEXT NOT NULL DEFAULT '';

UPDATE planets
SET
    type = 'terrestrial',
    interesting_fact = 'A day on Mercury lasts longer than its year. One solar day lasts about 176 Earth days, while Mercury completes an orbit in about 88 Earth days.'
WHERE name = 'Mercury';

UPDATE planets
SET
    type = 'terrestrial',
    interesting_fact = 'Venus rotates in the opposite direction to most planets, and its slow rotation makes a Venusian day longer than its year.'
WHERE name = 'Venus';

UPDATE planets
SET
    type = 'terrestrial',
    interesting_fact = 'Earth is the only world currently known to have life, and most of its surface is covered by liquid water.'
WHERE name = 'Earth';

UPDATE planets
SET
    type = 'terrestrial',
    interesting_fact = 'Mars is home to Olympus Mons, the largest known volcano in the Solar System, rising roughly 22 kilometres above the surrounding plains.'
WHERE name = 'Mars';

UPDATE planets
SET
    type = 'gas_giant',
    interesting_fact = 'Jupiter is so massive that its centre of mass with the Sun lies outside the Sun itself.'
WHERE name = 'Jupiter';

UPDATE planets
SET
    type = 'gas_giant',
    interesting_fact = 'Saturn has such a low average density that, in a hypothetical bathtub large enough to hold it, the planet would float.'
WHERE name = 'Saturn';

UPDATE planets
SET
    type = 'ice_giant',
    interesting_fact = 'Uranus rotates almost completely on its side, with an axial tilt of about 98 degrees, producing extreme seasonal changes.'
WHERE name = 'Uranus';

UPDATE planets
SET
    type = 'ice_giant',
    interesting_fact = 'Neptune has some of the fastest planetary winds known, reaching speeds of more than 2,000 kilometres per hour.'
WHERE name = 'Neptune';

ALTER TABLE planets
    ALTER COLUMN interesting_fact DROP DEFAULT;