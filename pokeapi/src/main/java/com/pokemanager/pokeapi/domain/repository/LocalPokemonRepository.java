package com.pokemanager.pokeapi.domain.repository;

import com.pokemanager.pokeapi.domain.model.LocalPokemon;

import java.util.Optional;
import java.util.UUID;

/**
 * Port (Clean Architecture): persistence contract for locally-synced Pokemon.
 * Implemented in the infrastructure layer — the domain knows nothing about JPA.
 */
public interface LocalPokemonRepository {

    LocalPokemon save(LocalPokemon pokemon);

    Optional<LocalPokemon> findById(UUID id);

    /** Duplicate-sync guard for US03: lookup by upstream PokeAPI id. */
    Optional<LocalPokemon> findByPokeApiId(int pokeApiId);

    boolean existsByPokeApiId(int pokeApiId);

    boolean existsByNameIgnoreCase(String name);
}
