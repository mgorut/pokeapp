/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.pokeapi;

import com.pokemanager.pokeapi.domain.model.EvolutionStage;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;
import com.pokemanager.pokeapi.domain.model.Statistics;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Infrastructure adapter implementing the {@link PokeApiClient} port against the
 * real PokeAPI v2 endpoints (https://pokeapi.co/api/v2).
 *
 * Endpoint mapping:
 * - US01 list      : GET /pokemon?limit&offset  + fan-out GET /pokemon/{id} per entry
 *                    (needed because the list payload lacks sprite/type/weight/abilities)
 * - US02 detail    : GET /pokemon/{id} + /pokemon-species/{id} + /evolution-chain/{id}
 *
 * Mass conversion: PokeAPI reports weight in hectograms -> kg = hg / 10.0.
 */
@Component
public class PokeApiClientAdapter implements PokeApiClient {

    private static final String POKEMON_LIST = "/pokemon";
    private static final int MAX_SKILLS = 6; // keep payloads lean: top abilities + first moves

    private final PokeApiHttpGateway http;

    public PokeApiClientAdapter(PokeApiHttpGateway http) {
        this.http = http;
    }

    @Override
    public int countTotal() {
        PokeApiDto.PokemonListResponse list =
                http.getRequired(POKEMON_LIST + "?limit=1&offset=0", PokeApiDto.PokemonListResponse.class);
        return list.count();
    }

    @Override
    public List<PokemonSummary> findPage(int page, int size) {
        String path = POKEMON_LIST + "?limit=" + size + "&offset=" + (page * size);
        PokeApiDto.PokemonListResponse list = http.getRequired(path, PokeApiDto.PokemonListResponse.class);
        if (list.results() == null) {
            return List.of();
        }
        List<PokemonSummary> summaries = new ArrayList<>();
        for (PokeApiDto.NamedApiResource ref : list.results()) {
            Integer id = ref.extractId();
            if (id == null) {
                continue;
            }
            PokeApiDto.PokemonFullResponse full =
                    http.getRequired("/pokemon/" + id, PokeApiDto.PokemonFullResponse.class);
            summaries.add(toSummary(full));
        }
        return summaries;
    }

    @Override
    public PokemonDetail fetchDetail(String idOrName) {
        PokeApiDto.PokemonFullResponse pokemon =
                http.getRequired("/pokemon/" + idOrName, PokeApiDto.PokemonFullResponse.class);
        PokeApiDto.SpeciesResponse species = http.getOptional(
                        "/pokemon-species/" + pokemon.id(), PokeApiDto.SpeciesResponse.class)
                .orElse(null);

        String narrative = extractEnglishFlavor(species);
        List<EvolutionStage> lineage = resolveLineage(species);

        return new PokemonDetail(
                pokemon.id(),
                pokemon.name(),
                pickImage(pokemon.sprites()),
                toStatistics(pokemon.stats()),
                narrative,
                lineage,
                false, // syncedLocally is applied by the application service
                null); // localUuid is applied by the application service
    }

    PokemonSummary toSummary(PokeApiDto.PokemonFullResponse p) {
        return new PokemonSummary(
                p.id(),
                p.name(),
                pickImage(p.sprites()),
                primaryType(p.types()),
                p.weight() == null ? null : p.weight() / 10.0, // hectograms -> kg
                collectSkills(p));
    }

    private String primaryType(List<PokeApiDto.TypeEntry> types) {
        if (types == null || types.isEmpty()) {
            return "Unknown";
        }
        return types.stream()
                .min(Comparator.comparingInt(PokeApiDto.TypeEntry::slot))
                .map(t -> capitalize(t.type().name()))
                .orElse("Unknown");
    }

    private List<String> collectSkills(PokeApiDto.PokemonFullResponse p) {
        List<String> skills = new ArrayList<>();
        if (p.abilities() != null) {
            p.abilities().forEach(a -> {
                if (a.ability() != null && a.ability().name() != null) {
                    skills.add(a.ability().name());
                }
            });
        }
        if (p.moves() != null) {
            for (PokeApiDto.MoveEntry m : p.moves()) {
                if (skills.size() >= MAX_SKILLS) {
                    break;
                }
                if (m.move() != null && m.move().name() != null) {
                    skills.add(m.move().name());
                }
            }
        }
        return skills.stream().distinct().limit(MAX_SKILLS).toList();
    }

    private String pickImage(PokeApiDto.Sprites sprites) {
        if (sprites == null) {
            return null;
        }
        if (sprites.officialArtwork() != null && sprites.officialArtwork().frontDefault() != null) {
            return sprites.officialArtwork().frontDefault();
        }
        return sprites.frontDefault();
    }

    private Statistics toStatistics(List<PokeApiDto.StatEntry> stats) {
        Map<String, Integer> byName = PokeApiDto.statsByName(stats);
        return new Statistics(
                byName.get("hp"),
                byName.get("attack"),
                byName.get("defense"),
                byName.get("special-attack"),
                byName.get("special-defense"),
                byName.get("speed"));
    }

    private String extractEnglishFlavor(PokeApiDto.SpeciesResponse species) {
        if (species == null || species.flavorTextEntries() == null) {
            return null;
        }
        return species.flavorTextEntries().stream()
                .filter(f -> f.language() != null && "en".equalsIgnoreCase(f.language().name()))
                .findFirst()
                .map(f -> f.flavorText().replaceAll("[\\n\\f\\r]", " "))
                .orElse(null);
    }

    /**
     * Chained call 2: evolution-chain/{id}, then walk the recursive chain depth-first,
     * assigning 1-based stage numbers. Sprites come from each node's own pokemon doc
     * (chain payload only carries name+url), so we reuse /pokemon/{name}.
     */
    List<EvolutionStage> resolveLineage(PokeApiDto.SpeciesResponse species) {
        if (species == null || species.evolutionChain() == null
                || species.evolutionChain().url() == null) {
            return List.of();
        }
        String chainPath = toRelativePath(species.evolutionChain().url());
        PokeApiDto.EvolutionChainResponse chain =
                http.getOptional(chainPath, PokeApiDto.EvolutionChainResponse.class).orElse(null);
        if (chain == null || chain.chain() == null) {
            return List.of();
        }
        List<EvolutionStage> stages = new ArrayList<>();
        walk(chain.chain(), 1, stages);
        return stages;
    }

    static String toRelativePath(String absolute) {
        int idx = absolute.indexOf("/api/v2");
        return idx >= 0 ? absolute.substring(idx + "/api/v2".length()) : absolute;
    }

    private void walk(PokeApiDto.ChainNode node, int stage, List<EvolutionStage> out) {
        if (node == null || node.species() == null) {
            return;
        }
        int pokeApiId = extractIdFromUrl(node.species().url());
        String sprite = http.getOptional("/pokemon/" + node.species().name(),
                        PokeApiDto.PokemonFullResponse.class)
                .map(p -> pickImage(p.sprites()))
                .orElse(null);
        out.add(new EvolutionStage(stage, pokeApiId, node.species().name(), sprite));
        if (node.evolvesTo() != null) {
            node.evolvesTo().forEach(next -> walk(next, stage + 1, out));
        }
    }

    private int extractIdFromUrl(String url) {
        if (url == null) {
            return 0;
        }
        String trimmed = url.replaceAll("/+$", "");
        int slash = trimmed.lastIndexOf('/');
        try {
            return Integer.parseInt(trimmed.substring(slash + 1));
        } catch (Exception e) {
            return 0;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
