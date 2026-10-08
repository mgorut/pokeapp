/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.model;

/** Core combat statistics as defined by the US02 contract. Missing stats are null, not 0. */
public record Statistics(Integer hp, Integer attack, Integer defense,
                         Integer specialAttack, Integer specialDefense, Integer speed) {
}
