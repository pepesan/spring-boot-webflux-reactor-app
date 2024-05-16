package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class ComplexReactorExamples {
    public static void main(String[] args) throws InterruptedException {
        Flux<Task> taskFlux = createTasks();
        ParallelFlux<String> parallelTaskFlux = taskFlux
                .parallel()
                .flatMap(task -> {
                    try {
                        return Flux.just(task.run())
                                .subscribeOn(Schedulers.boundedElastic());
                    } catch (InterruptedException e) {
                        return Flux.error(e);
                    }
                });
        Flux<String> sequentialTaskFlux = parallelTaskFlux.sequential();  // Convert to Flux

        sequentialTaskFlux
                .then(Mono.fromRunnable(() -> System.out.println("All tasks completed successfully!")))
                .subscribe();

        Thread.sleep(2000);
    }
    public static Flux<Task> createTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(() -> {
            // Task 1 logic
            Thread.sleep(1000);
            System.out.println("Executada 1");
            return "Uno";
        });
        tasks.add(() -> {
            Thread.sleep(1000);
            System.out.println("Executada 2");
            return "Dos";
        });
        tasks.add(() -> {
            Thread.sleep(1000);
            System.out.println("Executada 3");
            return "Tres";
        });
        return Flux.fromIterable(tasks);
    }
    @FunctionalInterface
    public interface Task {
        String run() throws InterruptedException;  // Now returns String
    }

}
