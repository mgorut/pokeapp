/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Jackson-bound DTOs mirroring the raw PokeAPI JSON shapes.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} on every record keeps the
 * adapter resilient to upstream schema additions (PokeAPI evolves frequently).
 */
final class PokeApiDto {

    private PokeApiDto() {
    }

    /** GET /pokemon?limit&offset — list envelope. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PokemonListResponse(int count, String next, String previous, List<NamedApiResource> results) {
    }

    /** Common {name, url} resource used across PokeAPI listings. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NamedApiResource(String name, String url) {
        /** Extract the numeric id from urls like https://pokeapi.co/api/v2/pokemon/1/ */
        Integer extractId() {
            if (url == null) {
                return null;
            }
            String trimmed = url.replaceAll("/+$", "");
            int slash = trimmed.lastIndexOf('/');
            try {
                return Integer.parseInt(trimmed.substring(slash + 1));
            } catch (Exception e) {
                return null;
            }
        }
    }

    /** GET /pokemon/{id}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PokemonDetailResponse(Integer id,
                                 String name,
                                 Integer weight, // hectograms in PokeAPI
                                 List<AbilityEntry> abilities,
                                 List<MoveEntry> moves,
                                 List<TypeEntry> types,
                                 Sprites sprites) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AbilityEntry(@JsonProperty("is_hidden") boolean hidden, AbilityRef ability) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AbilityRef(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MoveEntry(MoveRef move) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MoveRef(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TypeEntry(int slot, TypeRef type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TypeRef(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Sprites(@JsonProperty("front_default") String frontDefault,
                   @JsonProperty("official-artwork") OfficialArtwork officialArtwork) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OfficialArtwork(@JsonProperty("front_default") String frontDefault) {
    }

    /** GET /pokemon-species/{id}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record SpeciesResponse(@JsonProperty("flavor_text_entries") List<FlavorTextEntry> flavorTextEntries,
                           @JsonProperty("evolution_chain") EvolutionChainRef evolutionChain) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FlavorTextEntry(@JsonProperty("flavor_text") String flavorText, NamedApiResource language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EvolutionChainRef(String url) {
    }

    /** GET /evolution-chain/{id}. Chain nodes are recursive. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record EvolutionChainResponse(ChainNode chain) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChainNode(EvoSpecies species, @JsonProperty("evolves_to") List<ChainNode> evolvesTo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EvoSpecies(String name, String url) {
    }

    /** Generic stat payload (used for detail stats). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record StatEntry(@JsonProperty("base_stat") int baseStat, StatRef stat) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StatRef(String name, String url) {
    }

    /** Detail response WITH stats — separate record so PokemonDetailResponse stays lean above. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PokemonFullResponse(Integer id,
                               String name,
                               Integer weight,
                               List<AbilityEntry> abilities,
                               List<MoveEntry> moves,
                               List<TypeEntry> types,
                               List<StatEntry> stats,
                               Sprites sprites) {
    }

    /** Map of stat names to values helper kept out of records. */
    static Map<String, Integer> statsByName(List<StatEntry> stats) {
        if (stats == null) {
            return Map.of();
        }
        return stats.stream()
                .filter(s -> s.stat() != null && s.stat().name() != null)
                .collect(java.util.stream.Collectors.toMap(
                        s -> s.stat().name(), s -> s.baseStat(), (a, b) -> a));
    }
}
