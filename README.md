# 🎮 PokéManager

A full-stack Pokémon management app: browse the PokeAPI catalog, sync any Pokémon into your own local database, and enrich it with proprietary data (local names, geographic metadata, classification tags) — protected by JWT authentication and built with Clean Architecture on the backend.

---

## 🎯 Overview

PokéManager implements four user stories:

| # | User Story | Highlights |
|---|------------|------------|
| US01 | **Browse the catalog** – paginated list of all Pokémon from PokeAPI | Caffeine in-memory cache (24 h TTL) so repeated page requests never hammer the upstream API |
| US02 | **View details** – stats, types, sprites and the *full evolutionary lineage* | Recursive evolution-chain resolution, graceful `404`/`503` mapping |
| US03 | **Sync to local** – copy a remote Pokémon into the local DB | Idempotent (`409` if already synced), fills proprietary fields locally |
| US04 | **Enrich local data** – update proprietary fields | Optimistic locking (`@Version`) → concurrent writers get `409 Conflict` |

- **Backend** (`pokeapi/`): Java 17 · Spring Boot 3 · PostgreSQL 15 (+ H2 in tests) · Flyway · JPA · JWT (jjwt) · Caffeine · springdoc-openapi · JUnit 5 + Mockito
- **Frontend** (`pokeui/`): React 18 · TypeScript · Vite · Tailwind CSS · TanStack React Query · Axios (JWT interceptors) · React Hook Form + Zod · Vitest + Testing Library
- **DevOps**: multi-stage Dockerfiles, docker-compose orchestration with healthcheck-gated startup order, Nginx SPA host with `/api` reverse proxy

---

## 🏗️ Architecture

### Clean Architecture (backend)

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                           POKEAPI BACKEND (Spring Boot 3)                     │
│                                                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐    │
│  │  presentation/                                                        │    │
│  │  ┌───────────────────────────────────────────────────────────────┐    │    │
│  │  │ Controllers:                                                  │    │    │
│  │  │   • PublicPokemonController    – GET /api/public/pokemon*     │    │    │
│  │  │   • ProtectedPokemonController – POST/PUT /api/protected/*    │    │    │
│  │  │   • AuthController             – POST /api/auth/*             │    │    │
│  │  │  DTOs: RequestDtos, ResponseDtos, ErrorDto                    │    │    │
│  │  │  GlobalExceptionHandler        – unified error envelope       │    │    │
│  │  │  ▼  (HTTP layer only, no business logic)                      │    │    │
│  │  └───────────────────────────────────────────────────────────────┘    │    │
│  └────────────────────────────┬──────────────────────────────────────────┘    │
│                               │ calls use cases                               │
│                               ▼                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐    │
│  │  application/   (Use Cases / Services)                                │    │
│  │  ┌───────────────────────────────────────────────────────────────┐    │    │
│  │  │ PokemonEnumerationService   – US01: paginated catalog         │    │    │
│  │  │ PokemonDetailService        – US02: detail + evolution        │    │    │
│  │  │ PokemonSyncService          – US03: sync from PokeAPI         │    │    │
│  │  │ PokemonUpdateService        – US04: edit local data           │    │    │
│  │  │ AuthService                 – register/login/refresh/me       │    │    │
│  │  │  ▼  Orchestrates domain, owns @Transactional                  │    │    │
│  │  └───────────────────────────────────────────────────────────────┘    │    │
│  └────────────────────────────┬──────────────────────────────────────────┘    │
│                               │ uses domain models & ports                    │
│                               ▼                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐    │
│  │  domain/   (Pure Java – ZERO Spring/JPA imports)   CORE               │    │
│  │  ┌───────────────────────────────────────────────────────────────┐    │    │
│  │  │ Entities / Models:                                            │    │    │
│  │  │   • LocalPokemon    – aggregate root (UUID, version, tags)    │    │    │
│  │  │   • User            – auth domain model                       │    │    │
│  │  │   • PokemonDetail   – read-only detail + evolution            │    │    │
│  │  │   • EvolutionStage  – one node in lineage (stage, id, sprite) │    │    │
│  │  │   • PageResult<T>   – generic pagination envelope             │    │    │
│  │  │                                                               │    │    │
│  │  │ Ports (Interfaces – implemented by infrastructure):           │    │    │
│  │  │   • PokeApiClient          – fetch from PokeAPI               │    │    │
│  │  │   • LocalPokemonRepository – CRUD + findByPokeApiId           │    │    │
│  │  │   • UserRepository         – findByEmail, save                │    │    │
│  │  │   • PasswordEncoderPort    – encode/verify (BCrypt)           │    │    │
│  │  │   • JwtTokenProviderPort   – create/parse JWT                 │    │    │
│  │  │                                                               │    │    │
│  │  │ Domain Exceptions:                                            │    │    │
│  │  │   • PokemonNotFoundException, ConcurrentModificationException │    │    │
│  │  │   • PokeApiUnavailableException, InvalidPayloadException      │    │    │
│  │  │  ▼  Business rules, no framework coupling                     │    │    │
│  │  └───────────────────────────────────────────────────────────────┘    │    │
│  └────────────────────────────┬──────────────────────────────────────────┘    │
│                               │ implements ports                              │
│                               ▼                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐    │
│  │  infrastructure/   (Framework adapters)                               │    │
│  │  ┌───────────────────────────────────────────────────────────────┐    │    │
│  │  │ JPA Entities & Repositories:                                  │    │    │
│  │  │   • LocalPokemonEntity, UserEntity + Spring Data JPA          │    │    │
│  │  │   • LocalPokemonRepositoryAdapter (implements port)           │    │    │
│  │  │                                                               │    │    │
│  │  │ External API Gateway:                                         │    │    │
│  │  │   • PokeApiHttpGateway (Spring RestClient)                    │    │    │
│  │  │   • PokeApiClientAdapter  (implements PokeApiClient)          │    │    │
│  │  │   • DTOs: PokeApiDto.* (Jackson-mapped PokeAPI shapes)        │    │    │
│  │  │                                                               │    │    │
│  │  │ Cache & Config:                                               │    │    │
│  │  │   • Caffeine CacheConfig (24h TTL) for US01                   │    │    │
│  │  │   • Flyway migrations (V1 schema, V2 seed, V3 PG index)       │    │    │
│  │  │                                                               │    │    │
│  │  │ Security:                                                     │    │    │
│  │  │   • JwtTokenProvider (jjwt), BCryptPasswordEncoder            │    │    │
│  │  │   • SecurityConfig (filter chain, JWT filter, endpoints)      │    │    │
│  │  │  ▼  All Spring/JPA/JWT details contained here                 │    │    │
│  │  └───────────────────────────────────────────────────────────────┘    │    │
│  └────────────────────────────┬──────────────────────────────────────────┘    │
└───────────────────────────────┼───────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
           ┌─────────────────┐      ┌─────────────────┐
           │  POKEAPI.CO     │      │  POSTGRESQL 15  │
           │  /api/v2        │      │  (Flyway owned) │
           │  (REST/HTTPS)   │      │  H2 in tests    │
           └─────────────────┘      └─────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  DEPENDENCY RULE (points inward):                                           │
│  presentation → application → domain ← infrastructure                       │
│                                                                             │
│  • Domain has ZERO Spring/JPA imports (enforced by ArchUnit test)           │
│  • All business logic in domain/application – testable without Spring       │
│  • Infrastructure swaps (DB, cache, auth) don't touch domain                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Package | Responsibility | Tests |
|-------|---------|----------------|-------|
| **presentation** | `com.pokemanager.pokeapi.presentation` | HTTP in/out, serialization, exception mapping | Integration tests (`@SpringBootTest`) |
| **application** | `com.pokemanager.pokeapi.application.service` | Use cases, transactions, orchestration | Unit tests (mock ports) + Integration |
| **domain** | `com.pokemanager.pokeapi.domain` | **Pure business logic**, entities, ports, exceptions | Fast unit tests (no Spring context) |
| **infrastructure** | `com.pokemanager.pokeapi.infrastructure` | JPA, PokeAPI HTTP, Caffeine, JWT, Flyway | Integration tests |

The domain layer is framework-free (ArchUnit-verified), making business logic unit-testable with plain JUnit/Mockito — no Spring context startup needed.

### Frontend structure

`pokeui/src/features/` is organized by feature — `auth/`, `pokemon-list/`, `pokemon-detail/`, `pokemon-edit/` — over a shared UI kit (Button, Input, Card, Modal, Toast, Skeleton, Navbar). Auth lives in a React Context; server state in React Query; an Axios interceptor attaches the JWT and transparently refreshes it on `401`. Forms are validated client-side with Zod schemas that mirror the backend Bean Validation rules.

### Key design decisions

- **Domain purity over convenience** — repository/port interfaces live in `domain/`; JPA is an implementation detail confined to `infrastructure/`. Trade-off: adapter mapping code, payoff: fast, isolated tests.
- **Caffeine instead of Redis** — single-node demo app; an in-process cache avoids an extra container while still satisfying US01's caching nice-to-have.
- **Flyway owns the schema** (`ddl-auto=none`) — Hibernate `validate` cannot reconcile Postgres `jsonb` against a portable `String` mapping, so migrations (V1 schema, V2 seed, PG-only V3 index) are the single source of truth.
- **Optimistic locking for US04** — a `version` column + `ConcurrentModificationException → 409` beats pessimistic locks for a low-contention editing workload.
- **Upstream errors mapped, never leaked** — PokeAPI `404` → our `404 PokemonNotFoundException`; IO failures / `5xx` → `503 PokeApiUnavailableException` with a user-friendly payload from the `GlobalExceptionHandler`.
- **Single-origin in Docker** — Nginx proxies `/api/*` to the backend container, so the browser needs no CORS config or absolute API URLs.

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose v2 (recommended path)
- Java 17+ and Maven 3.9+ (local backend development)
- Node 18+ (local frontend development)

### Running with Docker (Recommended)

```bash
cp .env.example .env        # optional: tweak DB creds / JWT_SECRET
docker-compose up --build
```

Access:
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **API Docs (Swagger UI):** http://localhost:8080/swagger-ui.html
- **Health probe:** http://localhost:8080/actuator/health

Compose starts Postgres first (`pg_isready` healthcheck), then the backend (waits for `service_healthy`), then Nginx (waits until the API reports UP). Flyway creates the schema and loads the seed data automatically on first boot.

### Local Development

#### Backend

Requires a reachable PostgreSQL (or override `DB_URL` etc.). From `pokeapi/`:

```bash
cd pokeapi
mvn spring-boot:run          # or: mvn verify first to run the full test suite
```

#### Frontend

```bash
cd pokeui
npm install
npm run dev                  # http://localhost:5173, proxies /api to :8080
```

---

## 🔐 Demo Credentials

Seeded by Flyway migration `V2__seed.sql` (BCrypt-hashed password):

- **Email:** `demo@pokeapi.co`
- **Password:** `Demo123!`

The seed also pre-syncs **Bulbasaur**, **Charmander** and **Squirtle** with proprietary fields filled in, so you can try US04 editing immediately after logging in. You can also register new users via `POST /api/auth/register`.

---

## 📡 API Documentation

Browsable, generated docs: **http://localhost:8080/swagger-ui.html** (OpenAPI JSON at `/v3/api-docs`).

### Public endpoints (no auth)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/public/pokemon?page=0&size=20` | Paginated Pokémon catalog (cached, US01) |
| `GET` | `/api/public/pokemon/{idOrName}` | Detail view incl. stats & full evolution chain (US02) — `404` unknown, `503` PokeAPI down |

### Protected endpoints (`Authorization: Bearer <JWT>` required)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/protected/pokemon/{idOrName}/sync` | Sync a Pokémon from PokeAPI into the local DB (US03) — `409` if already synced |
| `PUT` | `/api/protected/pokemon/{id}` | Update proprietary fields: `localizedName`, `geographicMetadata`, `internalClassificationTags`, `version` (US04) — `409` on version conflict |

### Auth endpoints (`/api/auth`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/register` | Create user `{email, password}` |
| `POST` | `/api/auth/login` | Returns access + refresh JWTs |
| `POST` | `/api/auth/refresh` | Exchange refresh token for a new access token |
| `GET` | `/api/auth/me` | Current user profile (token check) |

Errors follow a consistent shape produced by the `GlobalExceptionHandler`: timestamp, status, message, and per-field validation details on `400`.

---

## 🧪 Testing

### Backend (44 tests: service unit tests + integration tests on H2 in PostgreSQL mode)

```bash
cd pokeapi
mvn test                     # unit + integration suites
mvn verify                   # includes JaCoCo coverage report
# report: pokeapi/target/site/jacoco/index.html
```

Business-logic coverage exceeds the 80% requirement: `application.service` ≈ **99%** instructions, `domain.model` 90%, `infrastructure.security` 91%. Integration tests cover the full HTTP stack including `401`/`403` JWT paths, sync/update round-trips and optimistic-locking conflicts.

### Frontend (13 tests across 3 files)

```bash
cd pokeui
npm install
npm test                     # vitest run
npm run build                # tsc -b && vite build (type-check + production bundle)
npm run lint                 # eslint (clean)
```

---

## 🤖 GenAI Usage

This project was built with a generative-AI coding assistant (Qwen Code with project skills in `.qwen/skills/`). Full documentation follows the template in [`docs/06-GENAI-TOOL-GUIDE.md`](docs/06-GENAI-TOOL-GUIDE.md); the exact prompts are in [`docs/prompt text/`](docs/prompt%20text/) and the interactive workflow in [`docs/task-am.md`](docs/task-am.md) or [`docs/task-im.md`](docs/task-im.md).

### Prompt used
Requirements were provided as six spec files (`docs/01`–`docs/06`): project overview, functional requirements (US01–US04), technical requirements (Clean Architecture, DB, testing), frontend requirements, delivery/DevOps, and the GenAI guide. The master prompt instructed the agent to implement each phase, ask clarifying questions at decision points, and verify with real builds/tests.

### Iterations (prompt → output → validation → correction cycle)
1. **Backend scaffold + domain layer** — AI generated the four-layer structure; validated by grepping the domain package for Spring/JPA imports (must be zero).
2. **Services & PokeAPI gateway** — AI initially fetched only the first evolution stage; corrected to recursive evolution-chain resolution (US02 requires the full lineage).
3. **Caching** — not implemented in the first pass; added `CacheConfig` (Caffeine, 24 h TTL) with `@Cacheable` on the enumeration service.
4. **jsonb tag round-trip bug** — the first `mvn test` run failed one integration test (4 seeded tags read back as 0): Hibernate re-read the `jsonb` column double-encoded. Fixed with `@JdbcTypeCode(Types.VARCHAR)` on the entity column plus defensive unwrapping in `LocalPokemonRepositoryAdapter.readTags()`. This is documented here because it is exactly the kind of issue AI output needs *execution-based* validation to catch.

### Validation process
- **Automated:** `mvn test` (44 green), `mvn verify` + JaCoCo thresholds, `tsc -b`, `vite build`, `vitest run` (13 green), `eslint` clean.
- **Manual:** layer-dependency review, error-path review (404/409/503), edge cases (empty tag lists, oversized payloads, expired tokens, concurrent updates).
- **Principle applied:** no AI suggestion was accepted until it compiled, passed tests, and matched the spec — AI accelerated scaffolding and boilerplate; humans owned architecture review, the bug fix above, and verification.

---

## 📁 Repository Layout

```
pokeapp/
├── pokeapi/           # Spring Boot backend (Clean Architecture) + Dockerfile
├── pokeui/            # React + TS frontend (Vite) + Dockerfile + nginx.conf
├── docker-compose.yml # postgres -> backend -> frontend, health-gated startup
├── .env.example       # DB creds, JWT_SECRET, POKEAPI_BASE_URL (copy to .env)
└── docs/              # Original specification files 01–06 + prompts
```

---

## ✅ Delivery Checklist

- [x] All 4 user stories implemented and tested (44 backend + 13 frontend tests green)
- [x] Clean Architecture with framework-free domain layer
- [x] JWT authentication (register/login/refresh/me), integration-tested incl. 401/403
- [x] Caffeine caching for the public listing (US01)
- [x] PokeAPI integration with explicit 404/503 error handling
- [x] Seed data auto-loaded via Flyway (demo user + 3 starters)
- [x] Business-logic test coverage > 80% (≈99%)
- [x] Multi-stage Dockerfiles (non-root backend user, actuator healthcheck; Nginx SPA fallback + `/api` proxy)
- [x] docker-compose with named volume, network isolation, `.env` variables, healthchecks, `service_healthy` startup ordering
- [x] No hardcoded secrets (`.env.example` provided; strong `JWT_SECRET` enforced ≥32 chars)
- [x] No symlinks or unrelated files in the repository

## ⚠️ Legal Notice and Copyright

This repository is **public solely for technical evaluation purposes** as part of a selection process.

The code is protected by copyright (**All Rights Reserved**). 
Any use, copying, modification, or distribution for any other purpose is strictly prohibited without the express authorization of the author. 
Please refer to the [LICENSE](LICENSE.md) file for more details.
