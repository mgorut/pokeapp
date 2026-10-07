package com.pokemanager.pokeapi.domain.model;

import java.util.List;

/**
 * US01 list projection: exactly the fields required by the enumeration contract
 * (sprite, category = primary type, mass in kg, skills = ability names).
 */
public record PokemonSummary(Integer id,
                             String name,
                             String sprite,
                             String category,
                             Double mass,
                             List<String> skills) {
    public PokemonSummary {
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
