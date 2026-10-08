/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import com.pokemanager.pokeapi.infrastructure.persistence.entity.LocalPokemonEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain port by delegating to Spring Data JPA and
 * mapping entity &lt;-&gt; domain. Tags are JSON (de)serialized with Jackson.
 */
@Component
public class LocalPokemonRepositoryAdapter implements LocalPokemonRepository {

    private final LocalPokemonJpaRepository jpa;
    private final ObjectMapper objectMapper;

    public LocalPokemonRepositoryAdapter(LocalPokemonJpaRepository jpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.objectMapper = objectMapper;
    }

    @Override
    public LocalPokemon save(LocalPokemon pokemon) {
        return toDomain(jpa.save(toEntity(pokemon)));
    }

    @Override
    public Optional<LocalPokemon> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<LocalPokemon> findByPokeApiId(int pokeApiId) {
        return jpa.findByPokeApiId(pokeApiId).map(this::toDomain);
    }

    @Override
    public boolean existsByPokeApiId(int pokeApiId) {
        return jpa.existsByPokeApiId(pokeApiId);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }

    LocalPokemon toDomain(LocalPokemonEntity e) {
        return new LocalPokemon(e.getId(), e.getPokeApiId(), e.getName(),
                e.getLocalizedName(), e.getGeographicMetadata(),
                readTags(e.getInternalClassificationTags()),
                e.getSyncedAt(), e.getVersion());
    }

    /**
     * Domain -> entity. The version IS copied: the domain aggregate owns the
     * business version counter (checked/incremented in applyUpdate) and this
     * column simply mirrors it — there is no JPA @Version on the field, so
     * Hibernate will not touch it.
     */
    LocalPokemonEntity toEntity(LocalPokemon d) {
        LocalPokemonEntity e = new LocalPokemonEntity();
        e.setId(d.getId());
        e.setPokeApiId(d.getPokeApiId());
        e.setName(d.getName());
        e.setLocalizedName(d.getLocalizedName());
        e.setGeographicMetadata(d.getGeographicMetadata());
        e.setInternalClassificationTags(writeTags(d.getInternalClassificationTags()));
        e.setSyncedAt(d.getSyncedAt());
        e.setVersion(d.getVersion());
        return e;
    }

    private List<String> readTags(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            var node = objectMapper.readTree(json);
            if (node.isTextual()) {
                node = objectMapper.readTree(node.asText());
            }
            if (node.isArray()) {
                return objectMapper.convertValue(node, new TypeReference<List<String>>() {});
            }
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String writeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags == null ? List.of() : tags);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize classification tags", ex);
        }
    }
}
