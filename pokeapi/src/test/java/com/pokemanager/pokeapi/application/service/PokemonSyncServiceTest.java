package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.AlreadySyncedException;
import com.pokemanager.pokeapi.domain.exception.PokeApiUnavailableException;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** US03 unit tests: happy-path persistence, duplicate guard, race translation, upstream errors. */
@ExtendWith(MockitoExtension.class)
class PokemonSyncServiceTest {

    @Mock PokeApiClient pokeApiClient;
    @Mock LocalPokemonRepository repository;

    PokemonSyncService service;

    @BeforeEach
    void setUp() {
        service = new PokemonSyncService(pokeApiClient, repository);
    }

    private static PokemonDetail bulbasaur() {
        return new PokemonDetail(1, "bulbasaur", "img", null, null, java.util.List.of(), false, null);
    }

    @Test
    @DisplayName("sync fetches upstream then persists with documented default proprietary fields")
    void happyPath() {
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(bulbasaur());
        when(repository.existsByPokeApiId(1)).thenReturn(false);
        when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

        LocalPokemon saved = service.sync("bulbasaur");

        ArgumentCaptor<LocalPokemon> captor = ArgumentCaptor.forClass(LocalPokemon.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        LocalPokemon persisted = captor.getValue();
        assertThat(persisted.getPokeApiId()).isEqualTo(1);
        assertThat(persisted.getName()).isEqualTo("bulbasaur");
        assertThat(persisted.getLocalizedName()).isNull();
        assertThat(persisted.getGeographicMetadata()).isEqualTo("No recorded sightings yet");
        assertThat(persisted.getInternalClassificationTags()).containsExactly("synced");
        assertThat(persisted.getVersion()).isZero();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("already-synced pokemon raises AlreadySyncedException (HTTP 409)")
    void duplicateGuard() {
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(bulbasaur());
        when(repository.existsByPokeApiId(1)).thenReturn(true);

        assertThatThrownBy(() -> service.sync("bulbasaur"))
                .isInstanceOf(AlreadySyncedException.class)
                .hasMessageContaining("bulbasaur");
    }

    @Test
    @DisplayName("unique-constraint race is translated into 409 instead of leaking a 500")
    void raceBecomesConflict() {
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(bulbasaur());
        when(repository.existsByPokeApiId(1)).thenReturn(false);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup key"));

        assertThatThrownBy(() -> service.sync("bulbasaur"))
                .isInstanceOf(AlreadySyncedException.class);
    }

    @Test
    @DisplayName("upstream outage propagates unchanged (mapped to HTTP 503 by advice)")
    void upstreamDown() {
        when(pokeApiClient.fetchDetail("any"))
                .thenThrow(new PokeApiUnavailableException("timeout"));

        assertThatThrownBy(() -> service.sync("any"))
                .isInstanceOf(PokeApiUnavailableException.class);
    }
}
