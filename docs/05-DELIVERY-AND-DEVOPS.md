<!--
  Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
  
  This source code is licensed under the Restricted Use License found in the
  LICENSE.md file in the root directory of this source tree.
-->

Prepare the project for containerized deployment and documentation.

---
- [ ] Multi-stage build (build with Maven, run with JRE 17)
- [ ] Non-root user for security
- [ ] Health check endpoint
- [ ] Optimized layer caching

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- [ ] Multi-stage build (build with Node, serve with Nginx)
- [ ] Optimize bundle size
- [ ] Configure Nginx for SPA routing
- [ ] Services: `postgres`, `backend`, `frontend`
- [ ] Named volumes for database persistence
- [ ] Environment variables via `.env` file
- [ ] Network isolation
- [ ] Health checks for all services
- [ ] Proper startup order (depends_on with condition: service_healthy)

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: ${DB_URL}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

---
- [ ] **Users**: At least 1 demo user
  - Username: `demo@pokeapi.co`
  - Password: `Demo123!` (BCrypt hashed)
  - Role: `USER`
- [ ] **Pokemon**: At least 3 pre-synced Pokemon with proprietary fields filled
  - Example: Bulbasaur, Charmander, Squirtle (the starters)
  - Each with `localizedName`, `geographicMetadata`, `internalClassificationTags`
- **Option A**: `data.sql` file (simple, for H2/Postgres)
- **Option B**: Flyway migration with seed data
- **Option C**: `CommandLineRunner` in Spring Boot (programmatic)

---
```markdown
Brief description of the project and its purpose.
- Clean Architecture diagram
- Tech stack explanation
- Design decisions
- Docker & Docker Compose
- Java 17+ (for local development)
- Node 18+ (for frontend local development)
\`\`\`bash
docker-compose up --build
\`\`\`
Access:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html
\`\`\`bash
cd backend
./mvnw spring-boot:run
\`\`\`
\`\`\`bash
cd frontend
npm install
npm run dev
\`\`\`
- Email: `demo@pokeapi.co`
- Password: `Demo123!`
- `GET /api/public/pokemon` - Paginated list
- `GET /api/public/pokemon/{id}` - Detail view
- `POST /api/protected/pokemon/{id}/sync` - Sync from PokeAPI
- `PUT /api/protected/pokemon/{id}` - Update local data
- `POST /api/auth/register`
- `POST /api/auth/login`
\`\`\`bash
./mvnw test
npm test
\`\`\`
Explain key architectural choices and trade-offs.
Document how AI tools were used (see GenAI guide).
```

---
- [ ] `docker-compose up` works out of the box
- [ ] Seed data is loaded automatically
- [ ] README is comprehensive and accurate
- [ ] No hardcoded secrets (use `.env`)
- [ ] Health checks pass for all services
- [ ] Repository is public and clean (no symlinks, no unrelated files)

---
Generate:
1. Backend `Dockerfile`
2. Frontend `Dockerfile`
3. `docker-compose.yml`
4. Seed data script/file
5. Complete `README.md`

Include comments explaining Docker best practices applied.
