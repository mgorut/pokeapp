package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.InvalidPayloadException;
import com.pokemanager.pokeapi.domain.model.User;
import com.pokemanager.pokeapi.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication use case: registration + credential verification.
 *
 * The password encoder is injected as the Spring Security abstraction, keeping
 * this service free of JWT/token concerns (those live in infrastructure/security).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register a new user. Email/username uniqueness enforced with 400-mapped errors. */
    public User register(String username, String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new InvalidPayloadException("email", "already registered");
        }
        if (userRepository.existsByUsername(username)) {
            throw new InvalidPayloadException("username", "already taken");
        }
        User user = new User(UUID.randomUUID(), username.trim(), email.trim().toLowerCase(),
                passwordEncoder.encode(rawPassword), User.DEFAULT_ROLE, Instant.now());
        return userRepository.save(user);
    }

    /**
     * Verify credentials. On failure throws a generic InvalidPayloadException so
     * attackers cannot enumerate which half of the pair is wrong (user enumeration).
     */
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new InvalidPayloadException("credentials", "invalid email or password"));
        return user;
    }
}
