package com.pokemanager.pokeapi.domain.exception;

/**
 * Domain exception raised by the optimistic-locking rule in {@code LocalPokemon.applyUpdate}.
 * Presentation layer maps this to HTTP 409 Conflict so clients can re-read and retry.
 */
public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException(String message) {
        super(message);
    }
}
