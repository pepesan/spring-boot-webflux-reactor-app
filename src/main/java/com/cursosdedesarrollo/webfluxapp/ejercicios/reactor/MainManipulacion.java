package com.cursosdedesarrollo.webfluxapp.ejercicios.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class MainManipulacion {
    public static void main(String[] args) {
        // Ejercicio 1: Manipulación de datos con Flux
        Flux.range(1, 10)
                .map(num -> num * 2)
                .filter(num -> num % 2 == 0)
                .subscribe(System.out::println);

        // Ejercicio 2: Manipulación de datos con Mono
        Mono.just("Hola")
                .map(str -> str + " Mundo!")
                .subscribe(System.out::println);

        // Ejercicio 3: Uso de flatMap con Flux
        Flux<List<Integer>> listOfLists = Flux.just(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Arrays.asList(7, 8, 9)
        );

        listOfLists.flatMap(Flux::fromIterable)
                .filter(num -> num > 5)
                .subscribe(System.out::println);

        // Ejercicio 4: Uso de take con Flux
        Flux.range(97, 26) // Códigos ASCII de 'a' a 'z'
                .map(code -> Character.toString((char) code.intValue()))
                .take(5)
                .subscribe(System.out::println);
    }
}
