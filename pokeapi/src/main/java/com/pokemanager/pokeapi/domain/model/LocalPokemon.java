/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Locally-owned replica of a PokeAPI Pokemon plus the three proprietary fields
 * that only exist in our database (localizedName, geographicMetadata, tags).
 *
 * Optimistic locking: {@code version} is checked on update so two concurrent
 * edits cannot silently overwrite each other (US04 defensive requirement).
 */
public class LocalPokemon {

    private final UUID id;
    private final int pokeApiId;
    private final String name;
    private String localizedName;
    private String geographicMetadata;
    private List<String> internalClassificationTags;
    private final Instant syncedAt;
    private long version;

    public LocalPokemon(UUID id, int pokeApiId, String name, String localizedName,
                        String geographicMetadata, List<String> internalClassificationTags,
                        Instant syncedAt, long version) {
        this.id = id;
        this.pokeApiId = pokeApiId;
        this.name = name;
        this.localizedName = localizedName;
        this.geographicMetadata = geographicMetadata;
        this.internalClassificationTags = internalClassificationTags == null
                ? List.of() : List.copyOf(internalClassificationTags);
        this.syncedAt = syncedAt;
        this.version = version;
    }

    /** Factory used by the sync use case: proprietary fields initialized to defaults. */
    public static LocalPokemon newlySynced(int pokeApiId, String name) {
        return new LocalPokemon(UUID.randomUUID(), pokeApiId, name,
                null,                              // localized name unknown until user edits it
                "No recorded sightings yet",       // documented default geographic metadata
                List.of("synced"),                 // documented default classification tag
                Instant.now(), 0L);
    }

    /**
     * US04 business rule: apply a modification only if no concurrent update happened.
     *
     * @throws com.pokemanager.pokeapi.domain.exception.ConcurrentModificationException
     *          when the caller's expected version is stale
     */
    public void applyUpdate(String localizedName, String geographicMetadata,
                            List<String> internalClassificationTags, long expectedVersion) {
        if (expectedVersion != this.version) {
            throw new com.pokemanager.pokeapi.domain.exception.ConcurrentModificationException(
                    "Local pokemon was modified concurrently (expected version "
                            + expectedVersion + ", actual " + this.version + ")");
        }
        this.localizedName = localizedName;
        this.geographicMetadata = geographicMetadata;
        this.internalClassificationTags = internalClassificationTags == null
                ? List.of() : List.copyOf(internalClassificationTags);
        this.version++;
    }

    public UUID getId()               { return id; }
    public int getPokeApiId()         { return pokeApiId; }
    public String getName()           { return name; }
    public String getLocalizedName()  { return localizedName; }
    public String getGeographicMetadata() { return geographicMetadata; }
    public List<String> getInternalClassificationTags() { return internalClassificationTags; }
    public Instant getSyncedAt()      { return syncedAt; }
    public long getVersion()          { return version; }
}
