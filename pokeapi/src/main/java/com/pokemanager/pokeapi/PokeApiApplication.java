package com.pokemanager.pokeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** PokéManager backend entry point. Component scan roots at this package, covering all layers. */
@SpringBootApplication
public class PokeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokeApiApplication.class, args);
    }
}
