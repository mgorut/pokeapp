/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.presentation.controller;

import com.pokemanager.pokeapi.application.service.PokemonDetailService;
import com.pokemanager.pokeapi.application.service.PokemonEnumerationService;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.PokemonDetailDto;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.PokemonPageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * US01 + US02 public endpoints. No authentication required (see SecurityConfig).
 * Query-param constraints are enforced via @Validated on the controller so bad
 * page/size values produce a 400 with field errors instead of silently clamping.
 */
@RestController
@RequestMapping("/api/public/pokemon")
@Validated
@Tag(name = "Public Pokemon", description = "Unauthenticated browsing endpoints (US01, US02)")
public class PublicPokemonController {

    private final PokemonEnumerationService enumerationService;
    private final PokemonDetailService detailService;

    public PublicPokemonController(PokemonEnumerationService enumerationService,
                                   PokemonDetailService detailService) {
        this.enumerationService = enumerationService;
        this.detailService = detailService;
    }

    @GetMapping
    @Operation(summary = "Paginated Pokemon catalog (cached 24h)")
    public PokemonPageDto list(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "must be >= 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "must be >= 1")
            @Max(value = 50, message = "must be <= 50") int size) {
        return PokemonPageDto.from(enumerationService.enumerate(page, size));
    }

    @GetMapping("/{idOrName}")
    @Operation(summary = "Full detail incl. stats, flavor text and evolution lineage")
    public PokemonDetailDto detail(@PathVariable String idOrName) {
        return PokemonDetailDto.from(detailService.getDetail(idOrName));
    }
}
