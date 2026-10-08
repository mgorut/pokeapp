package com.pokemanager.pokeapi.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemanager.pokeapi.domain.model.EvolutionStage;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;
import com.pokemanager.pokeapi.domain.model.Statistics;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration slice for the four user stories (US01-US04) through the REAL
 * Spring context: Flyway+H2 (seed data included), Security/JWT, caching,
 * controllers and the global exception handler. Only the upstream PokeAPI
 * port is mocked so no network is required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PokemonApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean PokeApiClient pokeApiClient;

    /** Login helper: returns the JWT of the seeded demo account. */
    private String demoToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@bla.com\",\"password\":\"Demo123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    // ------------------------------------------------------------------
    // US01 — enumeration (public, paginated, cached)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US01: public list returns page metadata and mapped summaries")
    void listPage() throws Exception {
        when(pokeApiClient.findPage(anyInt(), anyInt())).thenReturn(List.of(
                new PokemonSummary(1, "bulbasaur", "s1.png", "Seed", 6.9, List.of("overgrow")),
                new PokemonSummary(2, "ivysaur", "s2.png", "Seed", 13.0, List.of("overgrow"))));
        when(pokeApiClient.countTotal()).thenReturn(2);

        mvc.perform(get("/api/public/pokemon").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))   // DTO field is 'content'
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.content[0].mass").value(6.9));
    }

    @Test
    @DisplayName("US01: size above 50 is rejected with 400 + field error")
    void listRejectsOversizedPage() throws Exception {
        mvc.perform(get("/api/public/pokemon").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                // ErrorDto exposes a flat list of {field,message} objects.
                .andExpect(jsonPath("$.fields[?(@.field == 'size')].message")
                        .value(hasItem("must be <= 50")));
    }

    // ------------------------------------------------------------------
    // US02 — detail (stats, narrative, lineage)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US02: detail by id exposes statistics, narrative and lineage; syncedLocally from DB")
    void detailById() throws Exception {
        when(pokeApiClient.fetchDetail("1")).thenReturn(new PokemonDetail(
                1, "bulbasaur", "s1.png",
                new Statistics(45, 49, 49, 65, 65, 45),
                "A strange seed was planted on its back at birth.",
                List.of(new EvolutionStage(1, "bulbasaur", "s1.png"),
                        new EvolutionStage(2, "ivysaur", "s2.png")),
                false, null));
        // bulbasaur IS in the seed data -> syncedLocally must come out true.
        mvc.perform(get("/api/public/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statistics.hp").value(45))
                .andExpect(jsonPath("$.narrativeDescription").exists())
                .andExpect(jsonPath("$.evolutionaryLineage", hasSize(2)))
                .andExpect(jsonPath("$.evolutionaryLineage[1].name").value("ivysaur"))
                .andExpect(jsonPath("$.syncedLocally").value(true));
    }

    @Test
    @DisplayName("US02: unknown pokemon maps to 404 domain error")
    void detailNotFound() throws Exception {
        when(pokeApiClient.fetchDetail("99999")).thenThrow(
                new com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException("nope"));
        mvc.perform(get("/api/public/pokemon/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ------------------------------------------------------------------
    // US03 — sync (protected)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US03: authenticated sync persists record and returns 201 + Location")
    void syncCreatesRecord() throws Exception {
        // pikachu is NOT part of the seed set -> fresh sync allowed.
        when(pokeApiClient.fetchDetail("pikachu")).thenReturn(new PokemonDetail(
                25, "pikachu", "p.png",
                new Statistics(35, 55, 40, 50, 50, 90), "It keeps its tail raised...",
                List.of(), false, null));

        String token = demoToken();
        mvc.perform(post("/api/protected/pokemon/pikachu/sync").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/protected/pokemon/")))
                .andExpect(jsonPath("$.name").value("pikachu"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.internalClassificationTags", hasSize(1)));
    }

    @Test
    @DisplayName("US03: syncing a seeded pokemon conflicts with 409")
    void syncDuplicateConflict() throws Exception {
        // Sync service fetches upstream FIRST (validation step), then checks the
        // local store -> the port stub is required even though the DB guard fails.
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(new PokemonDetail(
                1, "bulbasaur", "s.png",
                new Statistics(45, 49, 49, 65, 65, 45), "seed", List.of(), true, null));

        String token = demoToken();
        mvc.perform(post("/api/protected/pokemon/bulbasaur/sync").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ------------------------------------------------------------------
    // US04 — local update (protected, optimistic locking)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US04: full edit round-trip on seeded bulbasaur (read version -> update -> read back)")
    void updateRoundTrip() throws Exception {
        String token = demoToken();

        // 1) Read-back of seeded record: proprietary fields are pre-filled by V2__seed.sql.
        String body = mvc.perform(get("/api/protected/pokemon/00000000-0000-0000-0000-000000000001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localizedName").value("Bulbasaurio"))
                .andExpect(jsonPath("$.internalClassificationTags", hasSize(4)))
                .andReturn().getResponse().getContentAsString();
        long version = json.readTree(body).get("version").asLong();

        // 2) Update with the version we just read -> succeeds, version increments.
        Map<String, Object> payload = Map.of(
                "localizedName", "Bulba",
                "geographicMetadata", "Johto - union cave",
                // 4 distinct tags: expected echo must keep all four after normalization
                "internalClassificationTags", List.of("  STARTER ", "starter", "johto", "gen-2"),
                "version", version);
        mvc.perform(put("/api/protected/pokemon/00000000-0000-0000-0000-000000000001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localizedName").value("Bulba"))
                // tags normalized server-side: trimmed, lower-cased, de-duplicated
                .andExpect(jsonPath("$.internalClassificationTags",
                        containsInAnyOrder("starter", "johto", "gen-2")))
                .andExpect(jsonPath("$.version").value(version + 1));

        // 3) Stale version now loses the race -> 409 concurrent modification.
        payload = Map.of(
                "localizedName", "Stale",
                "geographicMetadata", "nowhere",
                "internalClassificationTags", List.of("x"),
                "version", version);
        mvc.perform(put("/api/protected/pokemon/00000000-0000-0000-0000-000000000001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(payload)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("US04: blank localizedName and empty tag list produce per-field 400 errors")
    void updateValidationErrors() throws Exception {
        String token = demoToken();
        Map<String, Object> payload = Map.of(
                "localizedName", "",
                "internalClassificationTags", List.of(),
                "version", 0);
        mvc.perform(put("/api/protected/pokemon/00000000-0000-0000-0000-000000000001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[?(@.field == 'localizedName')]", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.fields[?(@.field == 'internalClassificationTags')]",
                        hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("US04: updating a non-existent local id returns 404")
    void updateUnknownId() throws Exception {
        String token = demoToken();
        Map<String, Object> payload = Map.of(
                "localizedName", "Ghost",
                "internalClassificationTags", List.of("boo"),
                "version", 0);
        mvc.perform(put("/api/protected/pokemon/00000000-0000-0000-0000-00000000dead")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("protected endpoints without a token are rejected with 401")
    void unauthenticatedSyncRejected() throws Exception {
        mvc.perform(post("/api/protected/pokemon/1/sync"))
                .andExpect(status().isUnauthorized());
    }
}
