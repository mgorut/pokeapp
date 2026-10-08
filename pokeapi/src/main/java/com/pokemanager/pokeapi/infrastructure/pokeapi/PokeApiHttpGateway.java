package com.pokemanager.pokeapi.infrastructure.pokeapi;

import com.pokemanager.pokeapi.domain.exception.PokeApiUnavailableException;
import com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Thin, resilient GET wrapper around {@link RestClient} for the PokeAPI base URL.
 *
 * Error contract (shared by all adapters):
 * - 404            -> PokemonNotFoundException
 * - other status / IO errors -> PokeApiUnavailableException (mapped to 503)
 * The upstream response is never parsed twice; typed retrieval keeps Jackson
 * mapping inside this infrastructure layer.
 */
@Component
public class PokeApiHttpGateway {

    private static final Logger log = LoggerFactory.getLogger(PokeApiHttpGateway.class);

    private final RestClient restClient;

    // Single constructor so Spring resolves the bean unambiguously; the base URL
    // is already bound to the injected RestClient bean (see RestClientConfig).
    public PokeApiHttpGateway(RestClient pokeApiRestClient) {
        this.restClient = pokeApiRestClient;
    }

    public <T> Optional<T> getOptional(String path, Class<T> type) {
        try {
            T body = restClient.get()
                    .uri(path)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            throw new NotFoundExceptionMarker();
                        }
                        throw new UpstreamStatusException(res.getStatusCode().value());
                    })
                    .body(type);
            return Optional.ofNullable(body);
        } catch (NotFoundExceptionMarker notFound) {
            return Optional.empty();
        } catch (UpstreamStatusException e) {
            log.warn("PokeAPI returned {} for {}", e.status, path);
            throw new PokeApiUnavailableException("PokeAPI responded with status " + e.status);
        } catch (PokeApiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            // timeouts, connection resets, malformed JSON — everything degrades to 503
            log.warn("PokeAPI call failed for {}: {}", path, e.getMessage());
            throw new PokeApiUnavailableException("PokeAPI is unreachable: " + e.getMessage(), e);
        }
    }

    public <T> T getRequired(String path, Class<T> type) {
        return getOptional(path, type)
                .orElseThrow(() -> PokemonNotFoundException.forIdentifier(path));
    }

    /** Internal control-flow marker so 404 becomes Optional.empty without logging noise. */
    private static final class NotFoundExceptionMarker extends RuntimeException {
    }

    private static final class UpstreamStatusException extends RuntimeException {
        private final int status;

        UpstreamStatusException(int status) {
            this.status = status;
        }
    }
}
