/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * US02 — Detailed view use case.
 *
 * Composes two ports: the upstream client for the aggregate, and the local
 * repository to flag whether the Pokemon has already been synced (drives the
 * "Sync to Local" button visibility on the frontend detail page).
 *
 * Deliberately NOT cached: details change rarely but staleness here would show
 * users outdated sync state; enumeration (the expensive fan-out) is where the
 * 24h cache pays off.
 */
@Service
public class PokemonDetailService {

    private final PokeApiClient pokeApiClient;
    private final LocalPokemonRepository localPokemonRepository;

    public PokemonDetailService(PokeApiClient pokeApiClient,
                                LocalPokemonRepository localPokemonRepository) {
        this.pokeApiClient = pokeApiClient;
        this.localPokemonRepository = localPokemonRepository;
    }

    /**
     * @param idOrName numeric id or case-insensitive name accepted by PokeAPI
     * @return detail enriched with {@code syncedLocally} and {@code localUuid} when applicable
     * @throws com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException
     *         when upstream answers 404 (propagated from the client adapter)
     */
    public PokemonDetail getDetail(String idOrName) {
        PokemonDetail base = pokeApiClient.fetchDetail(idOrName);
        boolean synced = base.id() != null && localPokemonRepository.existsByPokeApiId(base.id());
        String localUuid = null;
        if (synced && base.id() != null) {
            localUuid = localPokemonRepository.findByPokeApiId(base.id())
                    .map(LocalPokemon::getId)
                    .map(UUID::toString)
                    .orElse(null);
        }
        return new PokemonDetail(
                base.id(), base.name(), base.image(), base.statistics(),
                base.narrativeDescription(), base.evolutionaryLineage(), synced, localUuid);
    }
}
