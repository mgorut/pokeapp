package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.InvalidPayloadException;
import com.pokemanager.pokeapi.domain.model.User;
import com.pokemanager.pokeapi.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Auth unit tests: uniqueness rules, hashing, anti-enumeration error message. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("register hashes the password and normalizes email case")
    void registerHappyPath() {
        when(userRepository.existsByEmailIgnoreCase("Admin@BLA.com")).thenReturn(false);
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("Demo123!")).thenReturn("$2b$12$hash");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User user = service.register("admin", "Admin@BLA.com", "Demo123!");

        assertThat(user.getEmail()).isEqualTo("admin@bla.com");
        assertThat(user.getPasswordHash()).isEqualTo("$2b$12$hash");
        assertThat(user.getRole()).isEqualTo(User.DEFAULT_ROLE);
    }

    @Test
    @DisplayName("duplicate email rejected with field-scoped 400 error")
    void duplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register("abc", "a@b.com", "Passw0rd!"))
                .isInstanceOf(InvalidPayloadException.class)
                .satisfies(e -> assertThat(((InvalidPayloadException) e).getField()).isEqualTo("email"));
    }

    @Test
    @DisplayName("duplicate username rejected with field-scoped 400 error")
    void duplicateUsername() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> service.register("taken", "a@b.com", "Passw0rd!"))
                .isInstanceOf(InvalidPayloadException.class)
                .satisfies(e -> assertThat(((InvalidPayloadException) e).getField()).isEqualTo("username"));
    }

    @Test
    @DisplayName("wrong password yields generic credentials error (no enumeration leak)")
    void wrongPassword() {
        User stored = new User(UUID.randomUUID(), "demo", "demo@bla.com", "$2b$12$hash", "USER", Instant.now());
        when(userRepository.findByEmailIgnoreCase("demo@bla.com")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("bad", "$2b$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate("demo@bla.com", "bad"))
                .isInstanceOf(InvalidPayloadException.class)
                .hasMessage("invalid email or password");
    }

    @Test
    @DisplayName("unknown email produces the SAME generic error as a wrong password")
    void unknownEmailSameError() {
        when(userRepository.findByEmailIgnoreCase("ghost@bla.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("ghost@bla.com", "whatever"))
                .isInstanceOf(InvalidPayloadException.class)
                .hasMessage("invalid email or password");
    }

    @Test
    @DisplayName("valid credentials authenticate successfully")
    void validCredentials() {
        User stored = new User(UUID.randomUUID(), "demo", "demo@bla.com", "$2b$12$hash", "USER", Instant.now());
        when(userRepository.findByEmailIgnoreCase("demo@bla.com")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("Demo123!", "$2b$12$hash")).thenReturn(true);

        assertThat(service.authenticate("demo@bla.com", "Demo123!").getUsername()).isEqualTo("demo");
    }
}
