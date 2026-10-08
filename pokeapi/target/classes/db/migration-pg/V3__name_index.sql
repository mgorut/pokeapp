-- Performance optimization for case-insensitive name lookups. This is an
-- EXPRESSION index which only PostgreSQL supports; H2 (used by the test
-- profile) cannot parse it, so it lives in its own migration that the test
-- profile ignores via spring.flyway.ignore-migration-patterns=*:V3 (see
-- application-test.properties). Production/Postgres applies it normally.
CREATE INDEX idx_pokemon_local_name ON pokemon_local ((LOWER(name)));
