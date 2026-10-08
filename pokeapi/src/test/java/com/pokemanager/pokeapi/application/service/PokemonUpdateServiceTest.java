package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.ConcurrentModificationException;
import com.pokemanager.pokeapi.domain.exception.InvalidPayloadException;
import com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** US04 unit tests: existence, tag normalization, optimistic locking. */
@ExtendWith(MockitoExtension.class)
class PokemonUpdateServiceTest {

    @Mock LocalPokemonRepository repository;

    PokemonUpdateService service;
    final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PokemonUpdateService(repository);
    }

    private LocalPokemon existing(long version) {
        return new LocalPokemon(id, 1, "bulbasaur", null, "old", List.of("synced"),
                java.time.Instant.now(), version);
    }

    @Test
    @DisplayName("updates the three proprietary fields and bumps the version")
    void happyPath() {
        when(repository.findById(id)).thenReturn(Optional.of(existing(0)));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalPokemon updated = service.update(id, "  Bulba ", "Kanto, Route 1",
                List.of(" Grass ", "POISON", "grass"), 0);

        assertThat(updated.getLocalizedName()).isEqualTo("Bulba");
        assertThat(updated.getGeographicMetadata()).isEqualTo("Kanto, Route 1");
        // trimmed, lower-cased, de-duplicated
        assertThat(updated.getInternalClassificationTags()).containsExactly("grass", "poison");
        assertThat(updated.getVersion()).isEqualTo(1);
        verify(repository).save(updated);
    }

    @Test
    @DisplayName("unknown local id raises PokemonNotFoundException (HTTP 404)")
    void notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, "x", "y", List.of("t"), 0))
                .isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    @DisplayName("stale version raises ConcurrentModificationException (HTTP 409)")
    void staleVersion() {
        when(repository.findById(id)).thenReturn(Optional.of(existing(3)));

        assertThatThrownBy(() -> service.update(id, "x", "y", List.of("t"), 2))
                .isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    @DisplayName("all-blank tags are rejected with InvalidPayloadException (HTTP 400)")
    void blankTagsRejected() {
        when(repository.findById(id)).thenReturn(Optional.of(existing(0)));

        assertThatThrownBy(() -> service.update(id, "x", "y", List.of(" ", ""), 0))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("null tags are rejected (HTTP 400)")
    void nullTagsRejected() {
        when(repository.findById(id)).thenReturn(Optional.of(existing(0)));

        assertThatThrownBy(() -> service.update(id, "x", "y", null, 0))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("more than 10 distinct tags rejected (HTTP 400)")
    void tooManyTags() {
        when(repository.findById(id)).thenReturn(Optional.of(existing(0)));
        List<String> tags = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(i -> "tag" + i).toList();

        assertThatThrownBy(() -> service.update(id, "x", "y", tags, 0))
                .isInstanceOf(InvalidPayloadException.class);
    }
}
