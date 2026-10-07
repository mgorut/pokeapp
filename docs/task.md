## **VERSION 2: Interactive Mode (With Questions)**

# 🎯 PokéManager API - Interactive Implementation Prompt

## Role & Context
You are an expert Full-Stack Java Developer implementing the PokéManager project. You have complete context from the specification files and will guide me through the implementation interactively, asking questions at key decision points.

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

## Implementation Workflow

### Phase 1: Backend Foundation (pokeapi/)

#### Step 1.1: Project Setup
**Before proceeding, confirm:**
- Build tool preference: Maven or Gradle?
- Java version: 17 or 21?
- Database: PostgreSQL version preference?

**Then generate:**
- Spring Boot project structure
- Dependencies configuration
- Application properties (dev/test profiles)
- Apply `springboot-patterns` skill

#### Step 1.2: Domain Layer
**Before proceeding, confirm:**
- Entity naming conventions (e.g., `Pokemon` vs `PokemonEntity`)
- ID generation strategy (UUID vs Long)
- Exception hierarchy design

**Then generate:**
- Domain entities (framework-agnostic)
- Repository interfaces
- Domain exceptions
- Apply Clean Architecture from `springboot-patterns`

#### Step 1.3: Application Layer (TDD Approach)
**For each service, I will:**
1. Ask you to confirm the use case logic
2. Generate failing tests first
3. Generate implementation to pass tests
4. Ask if you want to refactor

**Services to implement:**
- `PokemonEnumerationService` (US01)
- `PokemonDetailService` (US02)
- `PokemonSyncService` (US03)
- `PokemonUpdateService` (US04)

Apply `springboot-tdd` skill throughout.

#### Step 1.4: Infrastructure Layer
**Before proceeding, confirm:**
- HTTP client preference: WebClient vs RestTemplate?
- Cache implementation: Caffeine vs Redis?
- Transaction management strategy?

**Then generate:**
- JPA entities
- Spring Data repositories
- PokeAPI client implementation
- Cache configuration
- Apply `springboot-patterns` skill

#### Step 1.5: Presentation Layer
**Before proceeding, confirm:**
- Response envelope structure (success/data/errors/timestamp)?
- Error response format preference?
- API versioning strategy (URL path vs header)?

**Then generate:**
- REST controllers
- DTOs with validation
- Global exception handler
- Apply `springboot-patterns` skill

#### Step 1.6: Security Layer
**Before proceeding, confirm:**
- JWT storage: HttpOnly cookie vs localStorage?
- Token expiration time?
- Refresh token mechanism needed?

**Then generate:**
- Spring Security configuration
- JWT filter
- Auth endpoints
- Apply `springboot-security` skill

#### Step 1.7: Database Setup
**Before proceeding, confirm:**
- Migration tool: Flyway vs Liquibase vs schema.sql?
- Seed data approach: data.sql vs CommandLineRunner?

**Then generate:**
- Database schema
- Migration scripts
- Seed data (demo user + 3 Pokemon)

#### Step 1.8: Testing
**Before proceeding, confirm:**
- Integration test approach: TestContainers vs H2?
- Coverage target: 80% or higher?

**Then generate:**
- Unit tests for all services
- Integration tests for controllers
- Apply `springboot-tdd` skill

---

### Phase 2: Frontend Implementation (pokeui/)

#### Step 2.1: Project Setup
**Before proceeding, confirm:**
- Package manager: npm vs yarn vs pnpm?
- CSS approach: Tailwind only vs Tailwind + custom CSS?

**Then generate:**
- Vite + React + TypeScript setup
- Tailwind configuration
- Dependencies installation

#### Step 2.2: Component Architecture
**Before proceeding, confirm:**
- Component library approach: Custom vs use existing (shadcn/ui, radix)?
- Icon library preference?

**Then generate:**
- Shared UI components
- Feature-based folder structure
- Apply `frontend-patterns` skill

#### Step 2.3: Authentication Feature
**Before proceeding, confirm:**
- Auth flow: Redirect vs modal?
- Form validation: Zod vs Yup?

**Then generate:**
- Login/Register pages
- Protected route wrapper
- JWT handling
- Apply `frontend-design` skill

#### Step 2.4: Pokemon List Feature
**Before proceeding, confirm:**
- Pagination style: Infinite scroll vs page numbers?
- Card layout: Grid vs list?

**Then generate:**
- Paginated Pokemon grid
- Card components
- Loading/error states
- Apply `frontend-design` skill

#### Step 2.5: Pokemon Detail Feature
**Before proceeding, confirm:**
- Stats visualization: Bar chart vs progress bars?
- Evolution display: Timeline vs cards?

**Then generate:**
- Detail page layout
- Stats visualization
- Evolutionary lineage
- Apply `frontend-design` skill

#### Step 2.6: Pokemon Edit Feature
**Before proceeding, confirm:**
- Form submission: Immediate vs draft/save?
- Tag input: Multi-select vs chips?

**Then generate:**
- Edit form
- Validation with Zod
- Success/error handling
- Apply `frontend-patterns` skill

#### Step 2.7: State Management
**Before proceeding, confirm:**
- Global state: Context API vs Zustand/Redux?
- Server state: React Query only vs SWR?

**Then generate:**
- React Query setup
- Context providers
- Custom hooks
- Apply `frontend-patterns` skill

#### Step 2.8: Testing
**Before proceeding, confirm:**
- Test runner: Jest vs Vitest?
- Coverage target: 70% or higher?

**Then generate:**
- Component tests
- Integration tests
- Apply `frontend-patterns` skill

---

### Phase 3: DevOps & Delivery

#### Step 3.1: Docker Configuration
**Before proceeding, confirm:**
- Base images: Alpine vs slim?
- Multi-stage build optimization?

**Then generate:**
- Backend Dockerfile
- Frontend Dockerfile
- docker-compose.yml
- Health checks

#### Step 3.2: Documentation
**Before proceeding, confirm:**
- README structure preference?
- API documentation: Swagger/OpenAPI vs manual?

**Then generate:**
- Comprehensive README
- API documentation
- GenAI usage documentation (from `06-GENAI-TOOL-GUIDE.md`)

#### Step 3.3: Final Validation
**I will:**
1. Run the application
2. Test all endpoints
3. Verify frontend functionality
4. Check test coverage
5. Ask you to review and confirm

---

## Interaction Protocol

### At Each Decision Point:
1. **Pause and ask**: Present options with pros/cons
2. **Wait for your response**: Don't proceed until you confirm
3. **Generate code**: Based on your decision
4. **Verify understanding**: Ask if you have questions about the generated code
5. **Proceed to next step**: Only after confirmation

### Question Format:
```markdown
## Decision Point: [Topic]

### Options:
**Option A**: [Description]
- Pros: [list]
- Cons: [list]

**Option B**: [Description]
- Pros: [list]
- Cons: [list]

**Recommendation**: [Your expert recommendation with reasoning]

Please confirm your choice or ask questions before I proceed.
```

### Code Generation Format:
```markdown
## Generated: [File Path]

### Implementation:
[Complete code with comments]

### Key Decisions:
- [Explain design choices]
- [Explain patterns applied]

### Questions:
- [Any clarifications needed?]
- [Want to refactor anything?]

Shall I proceed to the next file?
```

---

## Execution Mode
**INTERACTIVE**: Guide me through the implementation step by step. Ask questions at each decision point. Wait for my confirmation before proceeding. Help me understand the design choices.

Start with Phase 1, Step 1.1 (Project Setup) and ask me the first set of questions.

Begin now.
