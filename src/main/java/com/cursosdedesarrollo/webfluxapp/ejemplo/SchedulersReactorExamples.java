package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SchedulersReactorExamples {
    public static void main(String[] args) throws InterruptedException {
        // Scheduler.immediate(): Ejecuta la tarea en el mismo hilo que el que la invoca.
        // Scheduler.single(): Ejecuta la tarea en un único hilo dedicado.
        // Scheduler.parallel(): Ejecuta la tarea en un grupo de hilos de tamaño infinito.
        // Scheduler.elastic(): Ejecuta la tarea en un grupo de hilos de tamaño elástico, que se adapta a la carga de trabajo.
        System.out.println("single");
        Scheduler scheduler = Schedulers.newSingle("Uno");

        Flux<String> flux = Flux.just("Hola", "Mundo");

        flux.subscribeOn(scheduler)
                .subscribe(System.out::println);
        scheduler.dispose();

        System.out.println("parallel");
        scheduler = Schedulers.parallel();
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);
        numbers.subscribeOn(scheduler)
                .map(number -> {
                    // Simular una operación costosa
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return number;
                })
                .reduce(0, Integer::sum)
                .subscribe(sum -> System.out.println("Suma: " + sum));

        Thread.sleep(1000);
        scheduler.dispose();

        System.out.println("parallel web request");
        scheduler = Schedulers.parallel();
        String[] urls = {
                "https://aula.cursosdedesarrollo.com/",
                "https://cursosdedesarrollo.com/",
                "https://beta.cursosdedesarrollo.com"};
        Flux<String> urlsFlux = Flux.fromArray(urls);
        urlsFlux.subscribeOn(scheduler)
                .flatMap(url -> {
                    // Simular una llamada HTTP
                    HttpClient client = HttpClient.newHttpClient();
                    // Crear una solicitud HTTP GET
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();
                    // Enviar la solicitud y obtener la respuesta
                    HttpResponse<String> response = null;
                    try {
                        response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // Reemplazar con la lógica real para realizar la llamada
                    return Flux.just(response);
                })
                // ... resto del código para procesar el resultado (opcional)
                .subscribe(response -> {
                    System.out.println("Respuesta: " + response);
                    System.out.println("Status Code: " + response.statusCode());
                });
        Thread.sleep(10000);
        scheduler.dispose();
    }

}
