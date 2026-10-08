package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException;
import com.pokemanager.pokeapi.domain.model.EvolutionStage;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.model.Statistics;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** US02 unit tests: detail composition + syncedLocally flag + error propagation. */
@ExtendWith(MockitoExtension.class)
class PokemonDetailServiceTest {

    @Mock PokeApiClient pokeApiClient;
    @Mock LocalPokemonRepository localPokemonRepository;

    PokemonDetailService service;

    @BeforeEach
    void setUp() {
        service = new PokemonDetailService(pokeApiClient, localPokemonRepository);
    }

    private PokemonDetail upstream(boolean synced) {
        return new PokemonDetail(1, "bulbasaur", "sprite.png",
                new Statistics(45, 49, 49, 65, 65, 45),
                "An unusual grass-pokémon.",
                List.of(new EvolutionStage(1, "bulbasaur", "s1.png"),
                        new EvolutionStage(2, "ivysaur", "s2.png")),
                synced, null);
    }

    @Test
    @DisplayName("flags a pokemon that already exists locally")
    void marksSyncedPokemon() {
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(upstream(false));
        when(localPokemonRepository.existsByPokeApiId(1)).thenReturn(true);

        PokemonDetail d = service.getDetail("bulbasaur");

        assertThat(d.syncedLocally()).isTrue();
        assertThat(d.evolutionaryLineage()).hasSize(2);
        assertThat(d.statistics().hp()).isEqualTo(45);
    }

    @Test
    @DisplayName("unsynced pokemon yields syncedLocally=false (drives the Sync button)")
    void marksUnsyncedPokemon() {
        when(pokeApiClient.fetchDetail("charizard")).thenReturn(
                new PokemonDetail(6, "charizard", null, new Statistics(78, 84, 78, 109, 85, 100),
                        null, List.of(), false, null));
        when(localPokemonRepository.existsByPokeApiId(6)).thenReturn(false);

        assertThat(service.getDetail("charizard").syncedLocally()).isFalse();
        assertThat(service.getDetail("charizard").localUuid()).isNull();
    }

    @Test
    @DisplayName("upstream 404 propagates as PokemonNotFoundException (mapped to HTTP 404)")
    void propagatesNotFound() {
        when(pokeApiClient.fetchDetail("mew-999"))
                .thenThrow(PokemonNotFoundException.forIdentifier("mew-999"));

        assertThatThrownBy(() -> service.getDetail("mew-999"))
                .isInstanceOf(PokemonNotFoundException.class);
        verify(localPokemonRepository, never()).existsByPokeApiId(anyInt());
    }
}
