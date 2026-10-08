/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Application user. The password is ALWAYS stored as a BCrypt hash, never in clear text. */
public class User {

    public static final String DEFAULT_ROLE = "USER";

    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final Instant createdAt;

    public User(UUID id, String username, String email, String passwordHash,
                String role, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role == null ? DEFAULT_ROLE : role;
        this.createdAt = createdAt;
    }

    public UUID getId()                   { return id; }
    public String getUsername()           { return username; }
    public String getEmail()              { return email; }
    public String getPasswordHash()       { return passwordHash; }
    public String getRole()               { return role; }
    public Instant getCreatedAt()         { return createdAt; }
}
