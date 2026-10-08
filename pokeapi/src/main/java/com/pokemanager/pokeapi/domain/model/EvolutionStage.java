/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.model;

/** One node of the evolutionary lineage (1-based stage index). */
public record EvolutionStage(int stage, int pokeApiId, String name, String sprite) {
}
