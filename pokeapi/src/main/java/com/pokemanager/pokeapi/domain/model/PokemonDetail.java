package com.pokemanager.pokeapi.domain.model;

import java.util.List;

/**
 * US02 detailed view aggregate assembled from THREE chained PokeAPI calls:
 * pokemon/{id} + pokemon-species/{id} + evolution-chain/{id}.
 *
 * @param syncedLocally convenience flag telling an authenticated UI whether a "Sync" action is needed
 * @param localUuid UUID of the local record if syncedLocally is true, null otherwise
 */
public record PokemonDetail(Integer id,
                            String name,
                            String image,
                            Statistics statistics,
                            String narrativeDescription,
                            List<EvolutionStage> evolutionaryLineage,
                            boolean syncedLocally,
                            String localUuid) {
    public PokemonDetail {
        evolutionaryLineage = evolutionaryLineage == null ? List.of() : List.copyOf(evolutionaryLineage);
    }
}
