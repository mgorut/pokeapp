/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Stateless JWT issuance/validation (HS256).
 *
 * Design decisions:
 * - Secret + TTL come from configuration; production overrides via env vars so
 *   nothing sensitive is committed.
 * - Subject = username; email/role carried as custom claims so the auth filter
 *   can rebuild an authenticated principal without a DB round-trip per request.
 */
@Component
public class JwtTokenService {

    private final Key signingKey;
    private final Duration ttl;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.ttl:PT8H}") Duration ttl) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
        this.ttl = ttl;
    }

    public String generateToken(String username, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Parse & verify signature/expiry. Returns empty for ANY invalid token (never leaks why). */
    public Optional<Claims> parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}
