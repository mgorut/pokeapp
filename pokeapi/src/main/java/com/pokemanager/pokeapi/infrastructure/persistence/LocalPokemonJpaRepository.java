package com.pokemanager.pokeapi.infrastructure.persistence;

import com.pokemanager.pokeapi.infrastructure.persistence.entity.LocalPokemonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link LocalPokemonEntity}.
 *
 * Kept as a pure JPA-facing interface; the domain port (returning domain model
 * types) is implemented by {@code LocalPokemonRepositoryAdapter} which delegates
 * here and maps entity <-> domain. This avoids leaking JPA types into the port.
 */
public interface LocalPokemonJpaRepository extends JpaRepository<LocalPokemonEntity, UUID> {

    Optional<LocalPokemonEntity> findByPokeApiId(int pokeApiId);

    boolean existsByPokeApiId(int pokeApiId);

    @Query("select case when count(u) > 0 then true else false end from LocalPokemonEntity u "
            + "where lower(u.name) = lower(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
}
