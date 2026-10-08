/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.repository;

import com.pokemanager.pokeapi.domain.model.User;

import java.util.Optional;

/** Port: user persistence contract, implemented by the infrastructure layer. */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);
}
