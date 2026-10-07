package com.pokemanager.pokeapi.presentation.controller;

import com.pokemanager.pokeapi.application.service.AuthService;
import com.pokemanager.pokeapi.domain.model.User;
import com.pokemanager.pokeapi.domain.repository.UserRepository;
import com.pokemanager.pokeapi.infrastructure.security.JwtTokenService;
import com.pokemanager.pokeapi.presentation.dto.RequestDtos.LoginRequest;
import com.pokemanager.pokeapi.presentation.dto.RequestDtos.RegisterRequest;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.AuthResponseDto;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth endpoints: register/login are open, /me requires a JWT (route rules in
 * SecurityConfig). Login returns the bearer token plus its TTL so clients can
 * proactively refresh before expiry.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "JWT issuance and current-user lookup")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService tokenService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, JwtTokenService tokenService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Create a new user account")
    public ResponseEntity<UserInfoDto> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserInfoDto.from(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for a JWT")
    public AuthResponseDto login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String token = tokenService.generateToken(user.getUsername(), user.getEmail(), user.getRole());
        return AuthResponseDto.of(token, tokenService.expiresInSeconds(), user);
    }

    /** Nice-to-have: re-issue a token. Requires the caller's current JWT via Authorization header. */
    @PostMapping("/refresh")
    @Operation(summary = "Re-issue a JWT using the current valid JWT")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthResponseDto> refresh(Authentication authentication) {
        // /refresh is permitAll in SecurityConfig so an *invalid* token reaches us as anonymous;
        // we then answer 401 ourselves (same contract Spring would enforce on protected routes).
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("user vanished"));
        String token = tokenService.generateToken(user.getUsername(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(AuthResponseDto.of(token, tokenService.expiresInSeconds(), user));
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user info")
    @SecurityRequirement(name = "bearerAuth")
    public UserInfoDto me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("user vanished"));
        return UserInfoDto.from(user);
    }
}
