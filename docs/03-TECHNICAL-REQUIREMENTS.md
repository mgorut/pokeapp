# ⚙️ Technical Requirements - Architecture & Implementation

## Context
Refer to Project Overview and Functional Requirements. Implement the following technical components.

---

## 🗄️ DATABASE REQUIREMENTS

### Primary Entity: `pokemon_local`
- Unique primary key (UUID)
- Minimum 2 descriptive attributes (we have: `name`, `localized_name`, `geographic_metadata`, `internal_classification_tags`)
- Foreign reference to PokeAPI ID (unique constraint)

### Secondary Collection: `users` (User Management)
- Unique primary key (UUID)
- Minimum 2 descriptive attributes: `username`, `email`
- Additional required fields: `password_hash` (BCrypt), `created_at`, `roles`

### Schema Requirements
- [ ] Use Flyway or Liquibase for migrations (preferred) OR `data.sql` for seed
- [ ] Include indexes on frequently queried fields (`pokeapi_id`, `username`, `email`)
- [ ] Provide `schema.sql` and `data.sql` files

---

## 🔌 API REQUIREMENTS

### CRUD Operations
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

### Auxiliary Authentication API
- [ ] `POST /api/auth/register` - Create user
- [ ] `POST /api/auth/login` - Return JWT
- [ ] `POST /api/auth/refresh` - Refresh JWT (nice-to-have)
- [ ] `GET /api/auth/me` - Get current user info (protected)

### Route Protection
- [ ] Public routes: `/api/public/**`, `/api/auth/**`
- [ ] Protected routes: `/api/protected/**` (require valid JWT)
- [ ] Use Spring Security with JWT filter chain

---

## 🏛️ DATA LAYER REQUIREMENTS

### Specialized Data Access Layer
- [ ] Repository interfaces in Domain layer (framework-agnostic)
- [ ] Implementations in Infrastructure layer (Spring Data JPA)
- [ ] Custom query methods for complex searches
- [ ] Pagination support via `Pageable`
- [ ] Transaction management (`@Transactional` at service level)

---

## 🧠 CORE BUSINESS LOGIC REQUIREMENTS

### Dedicated Business Logic Layer
- [ ] Located in `application/service/` package
- [ ] Encapsulates ALL domain rules and validation
- [ ] **Architectural Independence**: 
  - MUST NOT import Spring annotations (except `@Service`)
  - MUST NOT import JPA annotations
  - MUST NOT import HTTP-related classes
  - Depends ONLY on Domain layer interfaces
- [ ] Use Dependency Injection via interfaces
- [ ] Throw domain-specific exceptions (e.g., `PokemonNotFoundException`, `InvalidPayloadException`)

### Example Structure
```java
// Domain layer
public interface PokemonRepository {
    Optional<Pokemon> findById(UUID id);
    Page<Pokemon> findAll(Pageable pageable);
}

// Application layer
@Service
public class PokemonSyncService {
    private final PokemonRepository repository;
    private final PokeApiClient pokeApiClient;
    
    public Pokemon syncFromPokeApi(String nameOrId) {
        // Business logic here - NO Spring/JPA imports
    }
}
```

---

## 🧪 TESTING AND VALIDATION REQUIREMENTS

### Unit Test Coverage
- [ ] **Every core component** must have unit tests:
  - Domain entities
  - Application services (use cases)
  - Infrastructure clients (PokeAPI)
  - REST controllers
- [ ] Use JUnit 5 + Mockito + AssertJ
- [ ] Mock external dependencies (PokeAPI, Database)
- [ ] Test both happy paths and edge cases

### Test Categories
1. **Unit Tests** (per class): Business logic, validation, transformations
2. **Integration Tests** (per module): Repository + DB, Controller + Service
3. **End-to-End Tests** (optional): Full flow with TestContainers

### TDD Approach
- [ ] Write failing test first
- [ ] Write minimal code to pass
- [ ] Refactor while keeping tests green
- [ ] Document the TDD cycle in README

### Coverage Target
- Minimum 80% on `application/` and `domain/` packages
- Minimum 70% overall project coverage

---

## 🛡️ ERROR HANDLING REQUIREMENTS

### Global Exception Handler
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

## Output Instructions
Generate the implementation in this order:
1. Database schema (Flyway migrations or SQL scripts)
2. Domain layer (entities, repository interfaces, exceptions)
3. Infrastructure layer (JPA implementations, PokeAPI client)
4. Application layer (services with business logic)
5. Presentation layer (controllers, DTOs, exception handler)
6. Security configuration (JWT filter, Spring Security config)
7. Tests for each layer

Include comprehensive comments explaining architectural decisions.
