package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ParallelFluxUrlsExample {
    public static void main(String[] args) {
        String[] urls = {
                "https://aula.cursosdedesarrollo.com/",
                "https://cursosdedesarrollo.com/",
                "https://beta.cursosdedesarrollo.com"
        };
        // Crear un ParallelFlux con un número definido de hilos
        ParallelFlux<HttpResponse<String>> parallelFlux = Flux.fromArray(urls)
                .parallel()
                .runOn(Schedulers.parallel()) // Ejecutar en hilos en paralelo
                .map(url -> {
                    System.out.println("Petición lanzada: " + url);
                    HttpResponse<String> response = makeRequest(url);
                    System.out.println("Respuesta: " + response);
                    System.out.println("Status code: " + response.statusCode());
                    System.out.println("Body trimmed: " + response.body().substring(0,200));
                    return response;
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
            Thread.sleep(10000); // Esperar un segundo para que las tareas se completen
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static HttpResponse<String> makeRequest(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (Exception e) {
            return new HttpResponse<String>() {
                @Override
                public int statusCode() {
                    return 0;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<String>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public String body() {
                    return "";
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public HttpClient.Version version() {
                    return null;
                }
            };
        }
    }
}
