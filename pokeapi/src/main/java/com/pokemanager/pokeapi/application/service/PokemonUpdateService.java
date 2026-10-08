package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.InvalidPayloadException;
import com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * US04 — Local data modification use case.
 *
 * Business rules enforced here (independent of HTTP concerns):
 * - target must exist locally (else 404-mapped exception);
 * - tags list is normalized (trimmed, lower-cased, de-duplicated) and rejected
 *   when empty after normalization (400-mapped exception);
 * - optimistic locking: caller supplies the version it read; mismatches raise
 *   ConcurrentModificationException (409-mapped) instead of silently clobbering.
 */
@Service
public class PokemonUpdateService {

    static final int MAX_TAGS = 10;

    private final LocalPokemonRepository localPokemonRepository;

    public PokemonUpdateService(LocalPokemonRepository localPokemonRepository) {
        this.localPokemonRepository = localPokemonRepository;
    }

    @Transactional
    public LocalPokemon update(UUID id,
                               String localizedName,
                               String geographicMetadata,
                               List<String> internalClassificationTags,
                               long expectedVersion) {

        LocalPokemon pokemon = localPokemonRepository.findById(id)
                .orElseThrow(() -> PokemonNotFoundException.forIdentifier("local id " + id));

        List<String> normalized = normalizeTags(internalClassificationTags);

        pokemon.applyUpdate(trimToNull(localizedName), trimToNull(geographicMetadata),
                normalized, expectedVersion);

        return localPokemonRepository.save(pokemon);
    }

    /** Trim/lowercase/dedupe; allow empty list (user may remove all tags). */
    private List<String> normalizeTags(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        List<String> normalized = raw.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase())
                .distinct()
                .toList();
        if (normalized.size() > MAX_TAGS) {
            throw new InvalidPayloadException("internalClassificationTags", "at most " + MAX_TAGS + " tags allowed");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
