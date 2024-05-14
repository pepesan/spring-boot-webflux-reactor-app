package com.cursosdedesarrollo.webfluxapp.ejercicios.reactor;

import reactor.core.publisher.Flux;

public class StringFluxCreator {
    public static Flux<String> createStringFlux() {
        return Flux.just("dato 1", "dato 2", "dato 3")
                .map(String::toUpperCase); // Modificar los datos a mayúsculas
    }
}
