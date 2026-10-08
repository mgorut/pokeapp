/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.exception;

/**
 * Domain exception: business-level validation failed on an incoming payload
 * (beyond what Bean Validation covers, e.g. duplicate tags after normalization).
 * Presentation layer maps this to HTTP 400 with field-level errors.
 */
public class InvalidPayloadException extends RuntimeException {

    private final String field;

    public InvalidPayloadException(String field, String message) {
        super(message);
        this.field = field;
    }

    public InvalidPayloadException(String message) {
        this("payload", message);
    }

    public String getField() {
        return field;
    }
}
