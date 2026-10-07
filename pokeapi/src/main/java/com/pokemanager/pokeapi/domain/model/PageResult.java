package com.pokemanager.pokeapi.domain.model;

import java.util.List;
import java.util.function.Function;

/**
 * Framework-agnostic pagination container.
 * The domain layer must not depend on Spring Data's {@code Pageable}/Page types,
 * so we model pagination ourselves and adapt at the infrastructure boundary.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content); // defensive immutability
    }

    /** Total number of pages given the current page size. */
    public int totalPages() {
        return size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /** Map the payload to another type (used to go domain -> DTO without leaking Spring). */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
