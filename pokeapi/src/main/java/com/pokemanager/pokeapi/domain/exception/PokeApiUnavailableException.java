/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

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
