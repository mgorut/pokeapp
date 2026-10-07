-- V2: Seed data required by the delivery spec (05-DELIVERY-AND-DEVOPS.md)
--  * one demo account: demo@bla.com / Demo123!  (BCrypt cost 12 hash below)
--  * three pre-synced Pokemon so the protected edit flow works out of the box.

INSERT INTO users (id, username, email, password_hash, role, created_at)
VALUES ('00000000-0000-0000-0000-000000000001',
        'demo',
        'demo@bla.com',
        '$2b$12$rVByh3ZAkR32RAbWWLbAq.WCYOgyhZ9xelXbxxIKmKjkJhuRyW2gW',
        'USER',
        TIMESTAMP '2024-01-01 00:00:00');

-- Bulbasaur (#1), Charmander (#4), Squirtle (#9).
-- internal_classification_tags is jsonb; we cast a JSON array literal explicitly
-- so the migration stays readable and reviewable.
INSERT INTO pokemon_local
    (id, pokeapi_id, name, localized_name, geographic_metadata,
     internal_classification_tags, synced_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000001', 1, 'bulbasaur',
     NULL, 'No recorded sightings yet', CAST('["synced"]' AS JSONB),
     TIMESTAMP '2024-01-01 00:00:00', 0),
    ('00000000-0000-0000-0000-000000000002', 4, 'charmander',
     NULL, 'No recorded sightings yet', CAST('["synced"]' AS JSONB),
     TIMESTAMP '2024-01-01 00:00:00', 0),
    ('00000000-0000-0000-0000-000000000003', 9, 'squirtle',
     NULL, 'No recorded sightings yet', CAST('["synced"]' AS JSONB),
     TIMESTAMP '2024-01-01 00:00:00', 0);
