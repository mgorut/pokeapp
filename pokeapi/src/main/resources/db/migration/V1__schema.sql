-- V1: users table (JWT authentication principals)
CREATE TABLE users (
    id            UUID PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL
);

-- V1: locally-synced Pokemon replica + proprietary fields (US03/US04)
-- internal_classification_tags is JSONB so raw SQL consumers can query tags directly;
-- the JPA adapter binds a JSON array string and we CAST in seed rows. Hibernate
-- validate-mode accepts jsonb for a String field via JDBC type resolution.
CREATE TABLE pokemon_local (
    id                          UUID PRIMARY KEY,
    pokeapi_id                  INT UNIQUE NOT NULL,
    name                        VARCHAR(100) NOT NULL,
    localized_name              VARCHAR(200),
    geographic_metadata         TEXT,
    internal_classification_tags JSONB,
    synced_at                   TIMESTAMP NOT NULL,
    version                     BIGINT NOT NULL DEFAULT 0
);
