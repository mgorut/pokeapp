package com.pokemanager.pokeapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * RestClient bound to the PokeAPI base URL with explicit connect/read timeouts.
 * Timeouts are mandatory: without them a hanging upstream would exhaust Tomcat
 * worker threads instead of degrading gracefully to HTTP 503.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient pokeApiRestClient(@Value("${pokeapi.base-url}") String baseUrl,
                                        @Value("${pokeapi.connect-timeout:PT5S}") Duration connectTimeout,
                                        @Value("${pokeapi.read-timeout:PT10S}") Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) connectTimeout.toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory((ClientHttpRequestFactory) factory)
                .build();
    }
}
