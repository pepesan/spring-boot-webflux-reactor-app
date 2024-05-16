package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ParallelHttpRequestReactor {
    public static void main(String[] args) throws InterruptedException {
        String[] urls = {
                "https://aula.cursosdedesarrollo.com/",
                "https://cursosdedesarrollo.com/",
                "https://beta.cursosdedesarrollo.com"
        };

        Flux.fromArray(urls)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(url -> Mono.fromCallable(() -> {
                    HttpResponse<String> response = makeRequest(url);
                    System.out.println("Respuesta: " + response);
                    System.out.println("Status code: " + response.statusCode());
                    return response;
                }))
                .sequential()
                .collectList()
                .subscribe(responseList -> {
                    for (HttpResponse<String> response: responseList){
                        System.out.println("Respuesta: " + response);
                        System.out.println("Status code: " + response.statusCode());
                    }
                    System.out.println("han finalizado todas");
                });

        Thread.sleep(5000);
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
