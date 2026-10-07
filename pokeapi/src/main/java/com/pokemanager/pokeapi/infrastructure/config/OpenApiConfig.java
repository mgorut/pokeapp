package com.pokemanager.pokeapi.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui.html — documents all endpoints incl. bearer auth. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pokemanagerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PokéManager API")
                        .version("1.0.0")
                        .description("Clean-Architecture Spring Boot backend for browsing, syncing and enriching Pokemon data from PokeAPI."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
