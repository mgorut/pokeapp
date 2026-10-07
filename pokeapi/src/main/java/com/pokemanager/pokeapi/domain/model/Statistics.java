package com.pokemanager.pokeapi.domain.model;

/** Core combat statistics as defined by the US02 contract. Missing stats are null, not 0. */
public record Statistics(Integer hp, Integer attack, Integer defense,
                         Integer specialAttack, Integer specialDefense, Integer speed) {
}
