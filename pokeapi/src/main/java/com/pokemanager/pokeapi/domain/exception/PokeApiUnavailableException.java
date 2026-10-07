package com.pokemanager.pokeapi.domain.exception;

/**
 * Domain exception: the upstream PokeAPI could not be reached (timeout / 5xx /
 * malformed response). Mapped to HTTP 503 Service Unavailable — we never expose
 * raw client stack traces to API consumers.
 */
public class PokeApiUnavailableException extends RuntimeException {

    public PokeApiUnavailableException(String message) {
        super(message);
    }

    public PokeApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
