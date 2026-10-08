/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.exception;

/**
 * Domain exception: US03 duplicate-sync guard. The Pokemon already has a local
 * record. Presentation layer maps this to HTTP 409 Conflict.
 */
public class AlreadySyncedException extends RuntimeException {

    public AlreadySyncedException(String name) {
        super("Pokemon '" + name + "' is already synced locally");
    }
}
