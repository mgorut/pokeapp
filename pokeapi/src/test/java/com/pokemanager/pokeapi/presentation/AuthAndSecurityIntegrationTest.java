package com.pokemanager.pokeapi.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end slice through the REAL Spring context (Flyway + H2 + Security + JWT).
 * Only the upstream PokeAPI port is mocked — everything else runs for real,
 * which also proves the seed migration (demo user) loads correctly.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndSecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    /** Replaces infrastructure PokeApiClientAdapter: no network in tests. */
    @MockBean PokeApiClient pokeApiClient;

    @Test
    @DisplayName("seeded demo user can log in and receive a JWT")
    void loginWithSeedUser() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@bla.com\",\"password\":\"Demo123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("demo"));
    }

    @Test
    @DisplayName("wrong password -> 400 generic error (no user enumeration)")
    void loginWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@bla.com\",\"password\":\"nope-nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }

    @Test
    @DisplayName("register validates payload with field errors")
    void registerValidation() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\",\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields", not(empty())));
    }

    @Test
    @DisplayName("protected route without token -> 401 JSON body")
    void protectedWithoutToken() throws Exception {
        mvc.perform(post("/api/protected/pokemon/bulbasaur/sync"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("protected route with garbage token -> 401")
    void protectedWithBadToken() throws Exception {
        mvc.perform(post("/api/protected/pokemon/bulbasaur/sync")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("full flow: login -> sync bulbasaur(seed) -> 409 duplicate")
    void syncDuplicateFromSeed() throws Exception {
        when(pokeApiClient.fetchDetail("bulbasaur")).thenReturn(
                new com.pokemanager.pokeapi.domain.model.PokemonDetail(
                        1, "bulbasaur", "img", null, null, List.of(), false, null));

        String token = obtainDemoToken();

        mvc.perform(post("/api/protected/pokemon/bulbasaur/sync")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already synced")));
    }

    @Test
    @DisplayName("GET /api/auth/me with valid token returns the demo user")
    void meEndpoint() throws Exception {
        String token = obtainDemoToken();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("demo@bla.com"));
    }

    @Test
    @DisplayName("public list endpoint works unauthenticated and uses the cache/mock client")
    void publicList() throws Exception {
        when(pokeApiClient.findPage(anyInt(), anyInt())).thenReturn(List.of(
                new com.pokemanager.pokeapi.domain.model.PokemonSummary(
                        1, "bulbasaur", "s.png", "Grass", 6.9, List.of("overgrow"))));
        when(pokeApiClient.countTotal()).thenReturn(1);

        mvc.perform(get("/api/public/pokemon?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private String obtainDemoToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@bla.com\",\"password\":\"Demo123!\"}"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }
}
