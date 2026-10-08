package com.pokemanager.pokeapi.domain.model;

/** One node of the evolutionary lineage (1-based stage index). */
public record EvolutionStage(int stage, int pokeApiId, String name, String sprite) {
}
