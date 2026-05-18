package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

class TemperatureControllerTest {

    private final TemperatureController controller = new TemperatureController();

    @Test
    void getTemperature_emitsIntegersInRange() {
        StepVerifier.withVirtualTime(() -> controller.getTemperature().take(3))
                .thenAwait(Duration.ofSeconds(3))
                .expectNextMatches(t -> t >= 0 && t < 50)
                .expectNextMatches(t -> t >= 0 && t < 50)
                .expectNextMatches(t -> t >= 0 && t < 50)
                .verifyComplete();
    }

    @Test
    void getTemperature_emitsWithOneSecondDelay() {
        StepVerifier.withVirtualTime(() -> controller.getTemperature().take(2))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(999))
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .verifyComplete();
    }
}
