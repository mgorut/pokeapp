package com.pokemanager.pokeapi.presentation.dto;

import com.pokemanager.pokeapi.domain.model.EvolutionStage;
import com.pokemanager.pokeapi.domain.model.LocalPokemon;
import com.pokemanager.pokeapi.domain.model.PageResult;
import com.pokemanager.pokeapi.domain.model.PokemonDetail;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;
import com.pokemanager.pokeapi.domain.model.Statistics;
import com.pokemanager.pokeapi.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTOs (presentation contract). Records with static factories keep the
 * domain <-> wire mapping in one place; the JSON shapes match the acceptance
 * criteria of US01-US04 exactly.
 */
public final class ResponseDtos {

    private ResponseDtos() {
    }

    /** US01 page envelope. */
    public record PokemonPageDto(List<PokemonSummaryDto> content,
                                 int page, int size,
                                 long totalElements, int totalPages) {

        public static PokemonPageDto from(PageResult<PokemonSummary> result) {
            return new PokemonPageDto(
                    result.content().stream().map(PokemonSummaryDto::from).toList(),
                    result.page(), result.size(), result.totalElements(), result.totalPages());
        }
    }

    public record PokemonSummaryDto(Integer id, String name, String sprite,
                                    String category, Double mass, List<String> skills) {

        public static PokemonSummaryDto from(PokemonSummary s) {
            return new PokemonSummaryDto(s.id(), s.name(), s.sprite(), s.category(),
                    s.mass(), s.skills());
        }
    }

    /** US02 detail. Field names follow the spec sample (specialAttack etc.). */
    public record PokemonDetailDto(Integer id, String name, String image,
                                   StatisticsDto statistics,
                                   String narrativeDescription,
                                   List<EvolutionStageDto> evolutionaryLineage,
                                   boolean syncedLocally) {

        public static PokemonDetailDto from(PokemonDetail d) {
            return new PokemonDetailDto(
                    d.id(), d.name(), d.image(),
                    d.statistics() == null ? null : StatisticsDto.from(d.statistics()),
                    d.narrativeDescription(),
                    d.evolutionaryLineage().stream().map(EvolutionStageDto::from).toList(),
                    d.syncedLocally());
        }
    }

    public record StatisticsDto(Integer hp, Integer attack, Integer defense,
                                Integer specialAttack, Integer specialDefense, Integer speed) {

        public static StatisticsDto from(Statistics s) {
            return new StatisticsDto(s.hp(), s.attack(), s.defense(),
                    s.specialAttack(), s.specialDefense(), s.speed());
        }
    }

    public record EvolutionStageDto(int stage, String name, String sprite) {

        public static EvolutionStageDto from(EvolutionStage e) {
            return new EvolutionStageDto(e.stage(), e.name(), e.sprite());
        }
    }

    /** US03/US04 local entity representation, including version for optimistic locking. */
    public record LocalPokemonDto(UUID uuid, int pokeapiId, String name,
                                  String localizedName, String geographicMetadata,
                                  List<String> internalClassificationTags,
                                  Instant syncedAt, long version) {

        public static LocalPokemonDto from(LocalPokemon p) {
            return new LocalPokemonDto(p.getId(), p.getPokeApiId(), p.getName(),
                    p.getLocalizedName(), p.getGeographicMetadata(),
                    p.getInternalClassificationTags(), p.getSyncedAt(), p.getVersion());
        }
    }

    /** Login response. */
    public record AuthResponseDto(String token, String tokenType, long expiresIn,
                                  UserInfoDto user) {

        public static AuthResponseDto of(String token, long expiresIn, User user) {
            return new AuthResponseDto(token, "Bearer", expiresIn, UserInfoDto.from(user));
        }
    }

    /** GET /api/auth/me + registration echo (never includes the hash). */
    public record UserInfoDto(UUID id, String username, String email, String role, Instant createdAt) {

        public static UserInfoDto from(User u) {
            return new UserInfoDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.getCreatedAt());
        }
    }

    /** Uniform error body incl. optional field-level errors (US04 requirement). */
    public record ErrorDto(int status, String error, String message, List<FieldErrorDto> fields) {

        public static ErrorDto of(int status, String error, String message) {
            return new ErrorDto(status, error, message, List.of());
        }

        public static ErrorDto withFields(int status, String error, String message, List<FieldErrorDto> fields) {
            return new ErrorDto(status, error, message, fields);
        }
    }

    public record FieldErrorDto(String field, String message) {
    }
}
