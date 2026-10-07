package com.pokemanager.pokeapi.presentation.controller;

import com.pokemanager.pokeapi.application.service.PokemonSyncService;
import com.pokemanager.pokeapi.application.service.PokemonUpdateService;
import com.pokemanager.pokeapi.domain.repository.LocalPokemonRepository;
import com.pokemanager.pokeapi.presentation.dto.RequestDtos.UpdatePokemonRequest;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.LocalPokemonDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * US03 + US04 protected endpoints — the whole controller sits under /api/protected/**
 * which Spring Security requires an authenticated JWT for (401 otherwise).
 */
@RestController
@RequestMapping("/api/protected/pokemon")
@Tag(name = "Protected Pokemon", description = "Authenticated sync/edit endpoints (US03, US04)")
@SecurityRequirement(name = "bearerAuth")
public class ProtectedPokemonController {

    private final PokemonSyncService syncService;
    private final PokemonUpdateService updateService;
    private final LocalPokemonRepository localPokemonRepository;

    public ProtectedPokemonController(PokemonSyncService syncService,
                                      PokemonUpdateService updateService,
                                      LocalPokemonRepository localPokemonRepository) {
        this.syncService = syncService;
        this.updateService = updateService;
        this.localPokemonRepository = localPokemonRepository;
    }

    @PostMapping("/{idOrName}/sync")
    @Operation(summary = "Copy a Pokemon from PokeAPI into the local database")
    public ResponseEntity<LocalPokemonDto> sync(@PathVariable String idOrName) {
        var created = syncService.sync(idOrName);
        // Location header points at the edit resource (US04 target id).
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/protected/pokemon/" + created.getId())
                .body(LocalPokemonDto.from(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update proprietary fields (optimistic locking via version)")
    public LocalPokemonDto update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdatePokemonRequest request) {
        var updated = updateService.update(id,
                request.localizedName(),
                request.geographicMetadata(),
                request.internalClassificationTags(),
                request.version());
        return LocalPokemonDto.from(updated);
    }

    /**
     * Read-back endpoint for the edit form: fetch a synced record (and its current
     * version) before modifying it. Without this the client could never obtain the
     * version needed for optimistic locking without re-syncing.
     */
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    @Operation(summary = "Fetch a locally-synced Pokemon by its local UUID")
    public ResponseEntity<LocalPokemonDto> getLocal(@PathVariable UUID id) {
        return localPokemonRepository.findById(id)
                .map(p -> ResponseEntity.ok(LocalPokemonDto.from(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
