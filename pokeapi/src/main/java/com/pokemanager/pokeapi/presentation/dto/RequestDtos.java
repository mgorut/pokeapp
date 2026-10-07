package com.pokemanager.pokeapi.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payloads with Bean Validation annotations mirroring the US04 rules:
 * - localizedName: @NotBlank @Size(max=200)
 * - geographicMetadata: @Size(max=1000)
 * - internalClassificationTags: @NotEmpty @Size(max=10 elements)
 * plus auth payloads (password complexity aligned with the seed demo account).
 */
public final class RequestDtos {

    private RequestDtos() {
    }

    /**
     * US04 update payload. {@code version} is REQUIRED so the server can detect
     * lost-update races (client must send back the version it last read).
     */
    public record UpdatePokemonRequest(
            @NotBlank(message = "must not be blank")
            @Size(max = 200, message = "must be at most 200 characters")
            String localizedName,

            @Size(max = 1000, message = "must be at most 1000 characters")
            String geographicMetadata,

            @NotEmpty(message = "must contain at least one tag")
            @Size(max = 10, message = "at most 10 tags allowed")
            List<@NotBlank(message = "tags must not be blank")
                   @Size(max = 50, message = "tag too long (max 50)") String> internalClassificationTags,

            @NotNull(message = "is required for optimistic locking (send the version you read)")
            Long version) {
    }

    public record RegisterRequest(
            @NotBlank(message = "must not be blank")
            @Size(min = 3, max = 50, message = "must be between 3 and 50 characters")
            String username,

            @NotBlank(message = "must not be blank")
            @Email(message = "must be a valid email")
            @Size(max = 150)
            String email,

            @NotBlank(message = "must not be blank")
            @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") // 72 = BCrypt input limit
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "must not be blank")
            @Email(message = "must be a valid email")
            String email,

            @NotBlank(message = "must not be blank")
            String password) {
    }
}
