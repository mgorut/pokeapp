/**
 * Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
 *
 * This source code is licensed under the Restricted Use License found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.pokemanager.pokeapi.application.service;

import com.pokemanager.pokeapi.domain.model.PageResult;
import com.pokemanager.pokeapi.domain.model.PokemonSummary;
import com.pokemanager.pokeapi.domain.port.PokeApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US01 unit tests: pagination clamping + cache behaviour.
 *
 * @Cacheable only takes effect through a Spring proxy, so we boot a tiny inline
 * annotation context (no web/JPA) that applies the real CacheInterceptor with a
 * key generator mirroring the production SpEL key, then wrap the service in an
 * AspectJProxyFactory. This proves "second identical request never re-hits
 * PokeAPI" without needing an HTTP mock or a full application boot.
 */
@ExtendWith(MockitoExtension.class)
class PokemonEnumerationServiceTest {

    @Mock
    private PokeApiClient pokeApiClient;

    /** Raw service instance — used for clamping assertions (bypasses cache). */
    private PokemonEnumerationService target;
    /** CGLIB proxy of the service with the real CacheInterceptor applied. */
    private Object cachedProxy;

    /** Local helper: cast the proxy once and call through it. */
    private PageResult<PokemonSummary> cached(int page, int size) {
        return ((PokemonEnumerationService) cachedProxy).enumerate(page, size);
    }

    @Configuration
    @EnableCaching
    static class TestCacheConfig implements CachingConfigurer {
        @Override
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(PokemonEnumerationService.CACHE_NAME);
        }

        @Override
        @Bean
        public org.springframework.cache.interceptor.KeyGenerator keyGenerator() {
            return (target, method, params) -> params[0] + "-" + params[1];
        }
    }

    @BeforeEach
    void setUp() {
        target = new PokemonEnumerationService(pokeApiClient);
        var ctx = new AnnotationConfigApplicationContext(TestCacheConfig.class);
        CacheInterceptor interceptor = ctx.getBean(CacheInterceptor.class);
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
        factory.addAdvice(interceptor);
        cachedProxy = factory.getProxy();
        ctx.close();
    }

    @Test
    @DisplayName("returns exactly the fields required by the enumeration contract")
    void enumerateReturnsContractFields() {
        when(pokeApiClient.findPage(0, 10)).thenReturn(List.of(
                new PokemonSummary(1, "bulbasaur", "img.png", "Grass", 6.9, List.of("overgrow"))));
        when(pokeApiClient.countTotal()).thenReturn(1000);

        PageResult<PokemonSummary> result = cached(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1000);
        assertThat(result.totalPages()).isEqualTo(100);
        PokemonSummary s = result.content().get(0);
        assertThat(s.sprite()).isEqualTo("img.png");
        assertThat(s.category()).isEqualTo("Grass");
        assertThat(s.mass()).isEqualTo(6.9);
        assertThat(s.skills()).containsExactly("overgrow");
    }

    @Test
    @DisplayName("second identical request is served from cache (no upstream call)")
    void secondRequestHitsCache() {
        when(pokeApiClient.findPage(anyInt(), anyInt())).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        cached(2, 20);
        cached(2, 20);

        verify(pokeApiClient, times(1)).findPage(2, 20);
        verify(pokeApiClient, times(1)).countTotal();
    }

    @Test
    @DisplayName("different pages are cached independently")
    void differentPagesAreSeparateKeys() {
        when(pokeApiClient.findPage(anyInt(), anyInt())).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        cached(0, 10);
        cached(1, 10);

        verify(pokeApiClient).findPage(0, 10);
        verify(pokeApiClient).findPage(1, 10);
    }

    @Test
    @DisplayName("page < 0 is clamped to 0 (business rule lives in the service)")
    void negativePageClamped() {
        when(pokeApiClient.findPage(0, 10)).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        PageResult<PokemonSummary> result = target.enumerate(-5, 10);

        verify(pokeApiClient).findPage(0, 10);
        assertThat(result.page()).isZero();
    }

    @Test
    @DisplayName("size > 50 is clamped to the hard upper bound")
    void oversizedPageSizeClamped() {
        when(pokeApiClient.findPage(0, 50)).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        target.enumerate(0, 999);

        verify(pokeApiClient).findPage(0, 50);
    }

    @Test
    @DisplayName("non-positive size falls back to the documented default of 10")
    void zeroSizeDefaultsToTen() {
        when(pokeApiClient.findPage(0, 10)).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        target.enumerate(0, 0);

        verify(pokeApiClient).findPage(0, 10);
    }

    @Test
    @DisplayName("no-arg overload applies defaults page=0 size=10")
    void defaultOverload() {
        when(pokeApiClient.findPage(0, 10)).thenReturn(List.of());
        when(pokeApiClient.countTotal()).thenReturn(0);

        PageResult<PokemonSummary> result = target.enumerate();

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
    }
}
