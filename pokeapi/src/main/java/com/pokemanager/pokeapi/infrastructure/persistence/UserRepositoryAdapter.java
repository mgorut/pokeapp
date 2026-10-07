package com.pokemanager.pokeapi.infrastructure.persistence;

import com.pokemanager.pokeapi.domain.model.User;
import com.pokemanager.pokeapi.domain.repository.UserRepository;
import com.pokemanager.pokeapi.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Adapter implementing the domain UserRepository port on top of Spring Data JPA. */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        return toDomain(jpa.save(toEntity(user)));
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return jpa.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getUsername(), e.getEmail(),
                e.getPasswordHash(), e.getRole(), e.getCreatedAt());
    }

    private UserEntity toEntity(User d) {
        UserEntity e = new UserEntity();
        e.setId(d.getId() == null ? UUID.randomUUID() : d.getId());
        e.setUsername(d.getUsername());
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setRole(d.getRole());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }
}
