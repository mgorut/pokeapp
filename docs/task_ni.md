## **VERSION 1: Autonomous Mode (No Questions)**

# 🎯 PokéManager API - Full Implementation Prompt

## Role & Context
You are an expert Full-Stack Java Developer implementing the PokéManager project. You have complete context from the specification files and will generate the entire solution autonomously without asking questions.

## Project Structure
```
.qwen/skills/     # Skills directory with patterns
pokeapi/          # Backend (Spring Boot)
pokeui/           # Frontend (React + TypeScript)
```

## Skills to Apply
### Backend Skills (from .qwen/skills/)
- `springboot-patterns`: Clean Architecture, REST best practices, error handling
- `springboot-security`: JWT authentication, route protection, Spring Security configuration
- `springboot-tdd`: Test-driven development, JUnit 5, Mockito, test coverage

### Frontend Skills (from .qwen/skills/)
- `frontend-design`: Responsive design, Tailwind CSS, user-centric UI
- `frontend-patterns`: React patterns, state management, component organization

## Specification Files (Already Available from docs/)
All requirements are defined in:
- `01-PROJECT-OVERVIEW.md` - Project context and tech stack
- `02-FUNCTIONAL-REQUIREMENTS.md` - 4 User Stories with acceptance criteria
- `03-TECHNICAL-REQUIREMENTS.md` - Architecture, DB, API, testing requirements
- `04-FRONTEND-REQUIREMENTS.md` - React frontend specifications
- `05-DELIVERY-AND-DEVOPS.md` - Docker, seed data, README requirements
- `06-GENAI-TOOL-GUIDE.md` - Documentation template

## Implementation Instructions

### Phase 1: Backend Foundation (pokeapi/)
1. **Project Setup**
   - Initialize Spring Boot 3.x project with Java 17
   - Configure PostgreSQL + H2 (for tests)
   - Set up Maven/Gradle dependencies
   - Apply `springboot-patterns` skill for project structure

2. **Domain Layer** (Zero framework dependencies)
   - Create entities: `Pokemon`, `User`
   - Define repository interfaces
   - Create domain exceptions: `PokemonNotFoundException`, `InvalidPayloadException`
   - Apply Clean Architecture principles from `springboot-patterns`

3. **Application Layer** (Business Logic)
   - Implement use case services:
     - `PokemonEnumerationService` (US01 - with caching)
     - `PokemonDetailService` (US02)
     - `PokemonSyncService` (US03)
     - `PokemonUpdateService` (US04)
   - Apply `springboot-tdd` skill: Write tests FIRST for each service
   - Mock PokeAPI client and repositories in tests

4. **Infrastructure Layer**
   - Implement JPA entities with `@Entity`, `@Table`
   - Create Spring Data JPA repositories
   - Implement `PokeApiClient` using WebClient/RestTemplate
   - Configure Spring Cache (Caffeine) for US01
   - Apply `springboot-patterns` for data access patterns

5. **Presentation Layer**
   - Create REST controllers for all endpoints
   - Implement DTOs with validation annotations
   - Create `GlobalExceptionHandler` with `@ControllerAdvice`
   - Map domain exceptions to HTTP status codes (400, 404, 401, 503)
   - Apply `springboot-patterns` for REST best practices

6. **Security Layer**
   - Configure Spring Security with JWT
   - Implement `JwtAuthenticationFilter`
   - Create auth endpoints: `/api/auth/register`, `/api/auth/login`
   - Protect routes: `/api/protected/**` requires JWT
   - Apply `springboot-security` skill for security patterns

7. **Database Setup**
   - Create Flyway migrations or `schema.sql`
   - Define tables: `users`, `pokemon_local`
   - Create `data.sql` with seed data:
     - 1 demo user (demo@bla.com / Demo123!)
     - 3 pre-synced Pokemon (Bulbasaur, Charmander, Squirtle)

8. **Testing**
   - Unit tests for all services (JUnit 5 + Mockito)
   - Integration tests for controllers and repositories
   - Target 80%+ coverage on business logic
   - Apply `springboot-tdd` skill for test patterns

### Phase 2: Frontend Implementation (pokeui/)
1. **Project Setup**
   - Initialize React 18 + Vite + TypeScript
   - Configure Tailwind CSS
   - Install dependencies: React Query, React Router, Axios, React Hook Form, Zod

2. **Component Architecture**
   - Create shared UI components (Button, Input, Card, Modal, Toast)
   - Organize features: `auth/`, `pokemon-list/`, `pokemon-detail/`, `pokemon-edit/`
   - Apply `frontend-patterns` skill for component organization

3. **Authentication Feature**
   - Login and Register pages
   - Protected route wrapper
   - JWT storage and Axios interceptors
   - Apply `frontend-design` skill for form design

4. **Pokemon List Feature**
   - Paginated grid with Pokemon cards
   - Display: sprite, name, category, mass, skills
   - Loading skeletons and error states
   - Apply `frontend-design` skill for responsive grid

5. **Pokemon Detail Feature**
   - Hero section with sprite
   - Stats visualization
   - Narrative description
   - Evolutionary lineage timeline
   - "Sync to Local" button (if authenticated)
   - Apply `frontend-design` skill for detail layout

6. **Pokemon Edit Feature**
   - Form with fields: localizedName, geographicMetadata, internalClassificationTags
   - Real-time validation with Zod
   - Success/error toasts
   - Apply `frontend-patterns` skill for form handling

7. **State Management**
   - React Query for server state (caching, mutations)
   - React Context for auth state
   - React Hook Form for form state
   - Apply `frontend-patterns` skill for state patterns

8. **Testing**
   - Component tests with Jest + React Testing Library
   - Test rendering, interactions, async states
   - Apply `frontend-patterns` skill for test patterns

### Phase 3: DevOps & Delivery
1. **Docker Configuration**
   - Backend `Dockerfile` (multi-stage build)
   - Frontend `Dockerfile` (multi-stage with Nginx)
   - `docker-compose.yml` with postgres, backend, frontend services
   - Health checks and proper startup order

2. **Documentation**
   - Comprehensive `README.md` with:
     - Project overview
     - Architecture diagram
     - Quick start (Docker + local dev)
     - API documentation
     - Demo credentials
     - Testing instructions
   - Apply `06-GENAI-TOOL-GUIDE.md` template for GenAI documentation

3. **Final Validation**
   - Run `docker-compose up` successfully
   - Verify all endpoints work
   - Check frontend displays correctly
   - Ensure seed data loads
   - Run all tests (backend + frontend)
   - No console warnings in browser

## Output Requirements

### For Each File Generated:
1. **Clear file path** (e.g., `pokeapi/src/main/java/com/bla/pokemon/domain/model/Pokemon.java`)
2. **Complete implementation** (no placeholders or TODOs)
3. **Inline comments** explaining design decisions
4. **Corresponding test file** (for backend)

### Code Quality Standards:
- Follow Clean Architecture strictly
- No framework dependencies in domain layer
- Comprehensive error handling
- Proper HTTP status codes
- Secure authentication
- Responsive UI
- No TypeScript errors
- No ESLint warnings

### Delivery Checklist:
- [ ] All 4 User Stories implemented and tested
- [ ] Clean Architecture with 4 layers (domain, application, infrastructure, presentation)
- [ ] JWT authentication working
- [ ] Caching implemented for US01
- [ ] PokeAPI integration with error handling
- [ ] Frontend consumes all endpoints
- [ ] Docker setup works out of the box
- [ ] Seed data pre-populated
- [ ] README is comprehensive
- [ ] Test coverage >80% on business logic
- [ ] No symlinks in repository

## Execution Mode
**AUTONOMOUS**: Generate the complete solution without asking questions. Make reasonable decisions based on the specifications. If you encounter ambiguity, choose the best practice approach.

Start with Phase 1, Step 1 (Backend Project Setup) and proceed sequentially through all phases.

Begin implementation now.
