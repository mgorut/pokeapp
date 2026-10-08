/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code pokemon_local} table (US03/US04).
 *
 * Design decisions:
 * - {@code @Version} column implements optimistic locking at the persistence level,
 *   complementing the domain-level version check in LocalPokemon.applyUpdate.
 * - Tags are stored as a JSON array string in a TEXT/JSONB-compatible column and
 *   (de)serialized by the mapper — avoids ElementCollection join tables and keeps
 *   the migration's JSONB column usable from raw SQL.
 */
@Entity
@Table(name = "pokemon_local")
public class LocalPokemonEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "pokeapi_id", unique = true, nullable = false)
    private int pokeApiId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "localized_name", length = 200)
    private String localizedName;

    @Column(name = "geographic_metadata", columnDefinition = "text")
    private String geographicMetadata;

    /**
     * JSON-encoded list of classification tags. Portable mapping: VARCHAR under
     * H2 (tests) and jsonb under Postgres (owned by the Flyway migration).
     * Hibernate binds it as a plain string, which Postgres accepts for jsonb
     * columns because varchar->jsonb is an implicit binary-coercible cast.
     *
     * NOTE ON READS: when Hibernate re-reads a jsonb column within the same
     * session that already persisted it, Postgres returns the binary jsonb
     * payload rendered as a quoted JSON *string* (e.g. "[\"a\",\"b\"]") rather
     * than the raw array text. readTags() in the adapter defensively unwraps
     * that double-encoding so tag lists survive an entity re-load.
     */
    @Column(name = "internal_classification_tags", columnDefinition = "TEXT", length = 4000)
    private String internalClassificationTags;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    /**
     * Optimistic locking is driven by the DOMAIN version (checked in
     * LocalPokemon.applyUpdate and incremented there). We deliberately do NOT
     * annotate this with @Version: the adapter persists whole aggregates built
     * from freshly-loaded domain objects, so Hibernate's own lock would double-
     * increment the counter on every save. The column value therefore always
     * mirrors the aggregate's business version, which clients round-trip.
     */
    @Column(name = "version", nullable = false)
    private long version;
    public LocalPokemonEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getPokeApiId() { return pokeApiId; }
    public void setPokeApiId(int pokeApiId) { this.pokeApiId = pokeApiId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocalizedName() { return localizedName; }
    public void setLocalizedName(String localizedName) { this.localizedName = localizedName; }

    public String getGeographicMetadata() { return geographicMetadata; }
    public void setGeographicMetadata(String geographicMetadata) { this.geographicMetadata = geographicMetadata; }

    public String getInternalClassificationTags() { return internalClassificationTags; }
    public void setInternalClassificationTags(String internalClassificationTags) { this.internalClassificationTags = internalClassificationTags; }

    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
