/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.AlreadySyncedException;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * US03 — Data synchronization use case.
 *
 * Flow (per acceptance criteria):
 *  1. Fetch full data from PokeAPI (also validates existence -> 404 upstream).
 *  2. Guard against duplicates -> AlreadySyncedException (HTTP 409).
 *  3. Persist local record with proprietary fields initialized to defaults.
 *
 * Concurrency note: the unique constraint on pokeapi_id is the ultimate guard;
 * if two requests race past the exists-check we translate the constraint
 * violation into the same 409 domain exception instead of leaking a 500.
 */
@Service
public class PokemonSyncService {

    private static final Logger log = LoggerFactory.getLogger(PokemonSyncService.class);

    private final PokeApiClient pokeApiClient;
    private final LocalPokemonRepository localPokemonRepository;

    public PokemonSyncService(PokeApiClient pokeApiClient,
                              LocalPokemonRepository localPokemonRepository) {
        this.pokeApiClient = pokeApiClient;
        this.localPokemonRepository = localPokemonRepository;
    }

    @Transactional
    public LocalPokemon sync(String idOrName) {
        PokemonDetail detail = pokeApiClient.fetchDetail(idOrName); // step 1 (may throw 404/503)

        if (localPokemonRepository.existsByPokeApiId(detail.id())) { // step 2
            throw new AlreadySyncedException(detail.name());
        }

        try {
            LocalPokemon created = LocalPokemon.newlySynced(detail.id(), detail.name()); // step 3
            return localPokemonRepository.save(created);
        } catch (DataIntegrityViolationException race) {
            log.warn("Duplicate sync race detected for pokeapi id {}", detail.id());
            throw new AlreadySyncedException(detail.name());
        }
    }
}
