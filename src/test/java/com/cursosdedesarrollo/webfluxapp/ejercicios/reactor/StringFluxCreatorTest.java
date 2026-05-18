package com.cursosdedesarrollo.webfluxapp.ejercicios.reactor;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class StringFluxCreatorTest {

    @Test
    void createStringFlux_emiteTresElementosEnMayusculas() {
        StepVerifier.create(StringFluxCreator.createStringFlux())
                .expectNext("DATO 1")
                .expectNext("DATO 2")
                .expectNext("DATO 3")
                .verifyComplete();
    }
}
