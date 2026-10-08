/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.model.PageResult;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * US01 — Pokemon Enumeration use case.
 *
 * Design decisions:
 * - Pagination bounds are enforced HERE (business rule), not only via Bean
 *   Validation, so every entry point gets the same guarantees.
 * - Caching: {@code @Cacheable} on a per-(page,size) key with a 24h TTL
 *   configured on the Caffeine cache (see CacheConfig). The annotation lives on
 *   the service bean so Spring's proxy applies it; the delegate call to the
 *   port is what actually hits PokeAPI on a cache miss.
 */
@Service
public class PokemonEnumerationService {

    /** Hard upper bound from the acceptance criteria. */
    static final int MAX_PAGE_SIZE = 50;
    static final int DEFAULT_PAGE_SIZE = 10;

    public static final String CACHE_NAME = "pokemonPage";

    private final PokeApiClient pokeApiClient;

    public PokemonEnumerationService(PokeApiClient pokeApiClient) {
        this.pokeApiClient = pokeApiClient;
    }

    /**
     * Cached enumeration page. Spring Cache key = page + size, so different pages
     * are cached independently and a repeated request within 24h never re-hits PokeAPI.
     */
    @Cacheable(value = CACHE_NAME, key = "#page + '-' + #size")
    public PageResult<PokemonSummary> enumerate(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = clampSize(size);

        List<PokemonSummary> content = pokeApiClient.findPage(safePage, safeSize);
        long total = pokeApiClient.countTotal();

        return new PageResult<>(content, safePage, safeSize, total);
    }

    /** Overload applying documented defaults (page 0, size 10). */
    public PageResult<PokemonSummary> enumerate() {
        return enumerate(0, DEFAULT_PAGE_SIZE);
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
