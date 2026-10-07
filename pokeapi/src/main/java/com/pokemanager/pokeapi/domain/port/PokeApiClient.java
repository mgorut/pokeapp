package com.pokemanager.pokeapi.domain.port;

import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;

import java.util.List;

/**
 * Port: abstraction over the upstream PokeAPI HTTP client.
 *
 * Keeping this as a plain interface lets the application-layer services be unit
 * tested with Mockito without any HTTP machinery, and allows swapping
 * RestTemplate/WebClient implementations at the infrastructure boundary.
 */
public interface PokeApiClient {

    /** Total number of Pokemon available upstream (used to compute pagination metadata). */
    int countTotal();

    /**
     * US01: fetch one page of summaries. Implementations may fan out detail calls
     * per summary entry to obtain sprite/category/mass/skills.
     */
    List<PokemonSummary> findPage(int page, int size);

    /**
     * US02/US03: full detail aggregate (pokemon + species flavor text + evolution chain).
     *
     * @throws com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException  on upstream 404
     * @throws com.pokemanager.pokeapi.domain.exception.PokeApiUnavailableException on timeout / 5xx
     */
    PokemonDetail fetchDetail(String idOrName);
}
