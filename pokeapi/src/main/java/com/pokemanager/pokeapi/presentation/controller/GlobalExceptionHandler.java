package com.pokemanager.pokeapi.presentation.controller;

import com.pokemanager.pokeapi.domain.exception.AlreadySyncedException;
import com.pokemanager.pokeapi.domain.exception.ConcurrentModificationException;
import com.pokemanager.pokeapi.domain.exception.InvalidPayloadException;
import com.pokemanager.pokeapi.domain.exception.PokeApiUnavailableException;
import com.pokemanager.pokeapi.domain.exception.PokemonNotFoundException;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.ErrorDto;
import com.pokemanager.pokeapi.presentation.dto.ResponseDtos.FieldErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Centralized domain-exception -> HTTP mapping (springboot-patterns):
 * - PokemonNotFoundException      -> 404
 * - InvalidPayloadException       -> 400 (with field-level error list)
 * - AlreadySyncedException        -> 409
 * - ConcurrentModificationException -> 409
 * - PokeApiUnavailableException   -> 503
 * Bean-validation failures produce the same ErrorDto shape so clients can parse
 * every error uniformly. Unknown exceptions log the stack trace but return a
 * sanitized 500 (no internal details leak).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(PokemonNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDto> userGone(UsernameNotFoundException e) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication no longer valid");
    }

    @ExceptionHandler({AlreadySyncedException.class, ConcurrentModificationException.class})
    public ResponseEntity<ErrorDto> conflict(RuntimeException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PokeApiUnavailableException.class)
    public ResponseEntity<ErrorDto> upstreamDown(PokeApiUnavailableException e) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Upstream PokeAPI is currently unavailable, please retry later");
    }

    @ExceptionHandler(InvalidPayloadException.class)
    public ResponseEntity<ErrorDto> invalidDomainPayload(InvalidPayloadException e) {
        ErrorDto body = ErrorDto.withFields(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(),
                List.of(new FieldErrorDto(e.getField(), e.getMessage())));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** @Valid body failures -> 400 with per-field messages (US04 acceptance criteria). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> validation(MethodArgumentNotValidException e) {
        List<FieldErrorDto> fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new FieldErrorDto(f.getField(), f.getDefaultMessage()))
                .toList();
        ErrorDto body = ErrorDto.withFields(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** @Validated method-parameter failures (e.g. page/size bounds) -> 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> constraint(ConstraintViolationException e) {
        List<FieldErrorDto> fields = e.getConstraintViolations().stream()
                .map(v -> new FieldErrorDto(leafPath(v.getPropertyPath()), v.getMessage()))
                .toList();
        ErrorDto body = ErrorDto.withFields(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Malformed JSON or wrong types in path/body -> 400 rather than 500. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorDto> unreadable(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request payload");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> unexpected(Exception e) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private String leafPath(Object propertyPath) {
        String full = propertyPath.toString();
        int dot = full.lastIndexOf('.');
        return dot >= 0 ? full.substring(dot + 1) : full;
    }

    private ResponseEntity<ErrorDto> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ErrorDto.of(status.value(), status.getReasonPhrase(), message));
    }
}
