package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

public class ParallelFluxExample {
    public static void main(String[] args) {
        // Crear un ParallelFlux con un número definido de hilos
        ParallelFlux<Integer> parallelFlux = Flux.range(1, 10)
                .parallel()
                .runOn(Schedulers.parallel()) // Ejecutar en hilos en paralelo
                .map(i -> {
                    System.out.println(i);
                    return i * 2;
                });

        // Verificar si todas las tareas en paralelo han sido completadas
        Mono<Boolean> allTasksCompleted = parallelFlux
                .then()
                .thenReturn(true) // Retorna true cuando todas las tareas están completadas
                .onErrorReturn(false); // Retorna false si hay algún error

        // Manejar el resultado de si todas las tareas en paralelo han sido completadas
        allTasksCompleted.subscribe(completed -> {
            if (completed) {
                System.out.println("Todas las tareas en paralelo han sido completadas.");
            } else {
                System.out.println("No todas las tareas en paralelo han sido completadas o ha ocurrido un error.");
            }
        });

        // Bloquear el hilo principal para que la aplicación no termine antes de que se completen las tareas
        try {
            Thread.sleep(1000); // Esperar un segundo para que las tareas se completen
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
