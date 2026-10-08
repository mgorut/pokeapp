/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * US01 caching: Caffeine behind the Spring Cache abstraction.
 * - TTL 24h per the acceptance criteria ("avoid redundant PokeAPI calls").
 * - maximumSize bounds heap usage since each cached page holds full summaries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POKEMON_PAGE_CACHE = "pokemonPage";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(24))
                .maximumSize(200));
        manager.setCacheNames(List.of(POKEMON_PAGE_CACHE));
        return manager;
    }
}
