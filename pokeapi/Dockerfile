# Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
# This source code is licensed under the Restricted Use License found in the
# LICENSE.md file in the root directory of this source tree.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
LABEL org.opencontainers.image.title="pokemanager-backend" \
      org.opencontainers.image.description="PokéManager Spring Boot API"
RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S appuser -G appgroup

WORKDIR /app
COPY --from=build /app/target/pokeapi-1.0.0.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
