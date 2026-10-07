package com.pokemanager.pokeapi.infrastructure.persistence;

import com.pokemanager.pokeapi.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA repository for {@link UserEntity}; wrapped by UserRepositoryAdapter. */
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);
}
