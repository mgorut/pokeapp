# syntax=docker/dockerfile:1
# ------------------------------------------------------------------
# Multi-stage build: compile with Maven + JDK 17, run on slim JRE 17.
# Layer-caching strategy: pom.xml is copied and dependencies resolved
# BEFORE the sources, so code changes never invalidate the (slow)
# dependency-download layer.
# ------------------------------------------------------------------

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (only re-downloaded when pom.xml changes).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now bring in sources and package. Tests already run in CI/local dev;
# the image build skips them for speed (mvn test is part of the README
# validation flow).
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
LABEL org.opencontainers.image.title="pokemanager-backend" \
      org.opencontainers.image.description="PokéManager Spring Boot API"

# Security: never run as root. Alpine uses addgroup/adduser.
RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S appuser -G appgroup

WORKDIR /app
# wget ships in busybox on alpine -> usable for the healthcheck below.
COPY --from=build /app/target/pokeapi-1.0.0.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Spring Actuator health endpoint (exposed in application.properties).
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
