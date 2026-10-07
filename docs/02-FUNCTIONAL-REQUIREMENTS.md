## 📄 File 2: `02-FUNCTIONAL-REQUIREMENTS.md`

# 📋 Functional Requirements - Pokemon User Stories

## Context
Refer to the Project Overview. Implement the following 4 User Stories with full Clean Architecture compliance and TDD approach.

---

## 🎯 USER STORY 01: Pokemon Enumeration

### Description
As an **unauthenticated user**, I want to browse Pokemon via paginated results so I can explore the catalog.

### Required Response Fields
Each Pokemon entry MUST include:
- `sprite` (URL to official artwork)
- `category` (e.g., "Grass", "Fire", "Water" - primary type)
- `mass` (weight in kg or lbs)
- `skills` (collection of abilities/moves names)

### Acceptance Criteria
- [ ] Endpoint: `GET /api/public/pokemon`
- [ ] Query params: `page` (default 0), `size` (default 10, max 50)
- [ ] Response structure:
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "bulbasaur",
        "sprite": "https://...",
        "category": "Grass",
        "mass": 6.9,
        "skills": ["overgrow", "chlorophyll"]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1302,
    "totalPages": 131
  }
  ```
- [ ] **Nice-to-have (REQUIRED for bonus)**: Implement Spring Cache (Caffeine) to avoid redundant PokeAPI calls. Cache TTL: 24 hours.
- [ ] Unit test: Verify pagination logic and cache hit/miss behavior.

---

## 🎯 USER STORY 02: Detailed View

### Description
As an **unauthenticated user**, I want to access comprehensive data for a specific Pokemon.

### Required Response Fields
- `image` (high-res sprite)
- `core statistics` (hp, attack, defense, spAtk, spDef, speed)
- `narrative description` (flavor text from pokemon-species endpoint)
- `evolutionary lineage` (full evolution chain with names and sprites)

### Acceptance Criteria
- [ ] Endpoint: `GET /api/public/pokemon/{idOrName}`
- [ ] Response structure:
  ```json
  {
    "id": 1,
    "name": "bulbasaur",
    "image": "https://...",
    "statistics": {
      "hp": 45,
      "attack": 49,
      "defense": 49,
      "specialAttack": 65,
      "specialDefense": 65,
      "speed": 45
    },
    "narrativeDescription": "A strange seed was planted...",
    "evolutionaryLineage": [
      {"stage": 1, "name": "bulbasaur", "sprite": "..."},
      {"stage": 2, "name": "ivysaur", "sprite": "..."},
      {"stage": 3, "name": "venusaur", "sprite": "..."}
    ]
  }
  ```
- [ ] Handle PokeAPI errors gracefully (timeout, 404, 503).
- [ ] Unit test: Mock PokeAPI client, verify lineage resolution (requires 2 chained HTTP calls: species -> evolution-chain).

---

## 🎯 USER STORY 03: Data Synchronization

### Description
As an **authenticated user**, I want to sync a Pokemon from PokeAPI to the local database so I can add proprietary fields.

### Proprietary Fields (MUST be included)
- `localizedName` (String) - e.g., regional name
- `geographicMetadata` (String) - e.g., "Found in Kanto region, tropical forests"
- `internalClassificationTags` (List<String>) - e.g., ["starter", "grass-type", "popular"]

### Acceptance Criteria
- [ ] Endpoint: `POST /api/protected/pokemon/{idOrName}/sync` (requires JWT)
- [ ] Behavior:
  1. Fetch full data from PokeAPI
  2. Create local record with proprietary fields initialized to defaults
  3. Return `201 Created` with the local entity
  4. If already synced, return `409 Conflict`
- [ ] Database table `pokemon_local`:
  ```sql
  CREATE TABLE pokemon_local (
    id UUID PRIMARY KEY,
    pokeapi_id INT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    localized_name VARCHAR(200),
    geographic_metadata TEXT,
    internal_classification_tags JSONB,
    synced_at TIMESTAMP DEFAULT NOW()
  );
  ```
- [ ] Unit test: Verify PokeAPI call, entity mapping, and duplicate prevention.

---

## 🎯 USER STORY 04: Local Data Modification

### Description
As an **authenticated user**, I want to update proprietary fields of a synced Pokemon.

### Acceptance Criteria
- [ ] Endpoint: `PUT /api/protected/pokemon/{id}` (requires JWT, `id` is local UUID)
- [ ] Request payload:
  ```json
  {
    "localizedName": "Bulbasaur (Español)",
    "geographicMetadata": "Bosques tropicales de Kanto",
    "internalClassificationTags": ["starter", "grass", "regional"]
  }
  ```
- [ ] Validation rules:
  - `localizedName`: @NotBlank, @Size(max=200)
  - `geographicMetadata`: @Size(max=1000)
  - `internalClassificationTags`: @NotEmpty, @Size(max=10)
- [ ] HTTP Status Codes:
  - `200 OK` on success
  - `400 Bad Request` for malformed/invalid payload (include field-level errors)
  - `404 Not Found` if Pokemon not synced locally
  - `401 Unauthorized` if no valid JWT
- [ ] Defensive logic: Optimistic locking to prevent concurrent updates.
- [ ] Unit test: Cover all validation scenarios and status codes.

---

## Output Instructions
Generate the implementation in this order:
1. **Domain layer**: Entities, Repository interfaces, Domain exceptions
2. **Application layer**: Use case services (one per User Story)
3. **Infrastructure layer**: JPA entities, Repository implementations, PokeAPI client
4. **Presentation layer**: REST controllers, DTOs, GlobalExceptionHandler
5. **Tests**: JUnit 5 + Mockito for each layer

For each file, include:
- Clear package structure
- JavaDoc comments
- Inline comments explaining design decisions
- Corresponding test class

Begin with the Domain layer.
