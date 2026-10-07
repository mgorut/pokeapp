package com.pokemanager.pokeapi.domain.exception;

/**
 * Domain exception: the requested Pokemon does not exist (upstream or locally).
 * Presentation layer maps this to HTTP 404. Pure Java - no framework dependency.
 */
public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String message) {
        super(message);
    }

    public static PokemonNotFoundException forIdentifier(String idOrName) {
        return new PokemonNotFoundException("Pokemon not found: " + idOrName);
    }
}
