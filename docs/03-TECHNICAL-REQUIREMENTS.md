<!--
  Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
  
  This source code is licensed under the Restricted Use License found in the
  LICENSE.md file in the root directory of this source tree.
-->

Refer to Project Overview and Functional Requirements. Implement the following technical components.

---
- Unique primary key (UUID)
- Minimum 2 descriptive attributes (we have: `name`, `localized_name`, `geographic_metadata`, `internal_classification_tags`)
- Foreign reference to PokeAPI ID (unique constraint)
- Unique primary key (UUID)
- Minimum 2 descriptive attributes: `username`, `email`
- Additional required fields: `password_hash` (BCrypt), `created_at`, `roles`
- [ ] Use Flyway or Liquibase for migrations (preferred) OR `data.sql` for seed
- [ ] Include indexes on frequently queried fields (`pokeapi_id`, `username`, `email`)
- [ ] Provide `schema.sql` and `data.sql` files

---
- [ ] Use standard HTTP verbs: GET, POST, PUT, DELETE
- [ ] Consistent return structures (envelope pattern):
  ```json
  {
    "success": true,
    "data": {},
    "errors": [],
    "timestamp": "2026-10-08T15:30:00Z"
  }
  ```
- [ ] Use proper content negotiation (application/json)
- [ ] Include HATEOAS links (nice-to-have)
- [ ] `POST /api/auth/register` - Create user
- [ ] `POST /api/auth/login` - Return JWT
- [ ] `POST /api/auth/refresh` - Refresh JWT (nice-to-have)
- [ ] `GET /api/auth/me` - Get current user info (protected)
- [ ] Public routes: `/api/public/**`, `/api/auth/**`
- [ ] Protected routes: `/api/protected/**` (require valid JWT)
- [ ] Use Spring Security with JWT filter chain

---
- [ ] Repository interfaces in Domain layer (framework-agnostic)
- [ ] Implementations in Infrastructure layer (Spring Data JPA)
- [ ] Custom query methods for complex searches
- [ ] Pagination support via `Pageable`
- [ ] Transaction management (`@Transactional` at service level)

---
- [ ] Located in `application/service/` package
- [ ] Encapsulates ALL domain rules and validation
- [ ] **Architectural Independence**: 
  - MUST NOT import Spring annotations (except `@Service`)
  - MUST NOT import JPA annotations
  - MUST NOT import HTTP-related classes
  - Depends ONLY on Domain layer interfaces
- [ ] Use Dependency Injection via interfaces
- [ ] Throw domain-specific exceptions (e.g., `PokemonNotFoundException`, `InvalidPayloadException`)
```java
public interface PokemonRepository {
    Optional<Pokemon> findById(UUID id);
    Page<Pokemon> findAll(Pageable pageable);
}
@Service
public class PokemonSyncService {
    private final PokemonRepository repository;
    private final PokeApiClient pokeApiClient;
    
    public Pokemon syncFromPokeApi(String nameOrId) {
    }
}
```

---
- [ ] **Every core component** must have unit tests:
  - Domain entities
  - Application services (use cases)
  - Infrastructure clients (PokeAPI)
  - REST controllers
- [ ] Use JUnit 5 + Mockito + AssertJ
- [ ] Mock external dependencies (PokeAPI, Database)
- [ ] Test both happy paths and edge cases
1. **Unit Tests** (per class): Business logic, validation, transformations
2. **Integration Tests** (per module): Repository + DB, Controller + Service
3. **End-to-End Tests** (optional): Full flow with TestContainers
- [ ] Write failing test first
- [ ] Write minimal code to pass
- [ ] Refactor while keeping tests green
- [ ] Document the TDD cycle in README
- Minimum 80% on `application/` and `domain/` packages
- Minimum 70% overall project coverage

---
- [ ] Use `@ControllerAdvice` + `@ExceptionHandler`
- [ ] Map domain exceptions to HTTP status codes:
  - `PokemonNotFoundException` → 404
  - `InvalidPayloadException` → 400
  - `UnauthorizedException` → 401
  - `PokeApiUnavailableException` → 503
  - `GenericException` → 500
- [ ] Return structured error response:
  ```json
  {
    "success": false,
    "errors": [
      {"field": "localizedName", "message": "must not be blank"}
    ],
    "timestamp": "2026-10-08T15:30:00Z"
  }
  ```

---
Generate the implementation in this order:
1. Database schema (Flyway migrations or SQL scripts)
2. Domain layer (entities, repository interfaces, exceptions)
3. Infrastructure layer (JPA implementations, PokeAPI client)
4. Application layer (services with business logic)
5. Presentation layer (controllers, DTOs, exception handler)
6. Security configuration (JWT filter, Spring Security config)
7. Tests for each layer

Include comprehensive comments explaining architectural decisions.
