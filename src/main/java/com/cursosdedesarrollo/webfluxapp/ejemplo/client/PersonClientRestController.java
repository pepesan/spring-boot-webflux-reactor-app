package com.cursosdedesarrollo.webfluxapp.ejemplo.client;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/persons/client")
public class PersonClientRestController {
    private static final String API_MIME_TYPE = "application/json";
    private static final String API_BASE_URL = "http://localhost:8081";
    private static final String USER_AGENT = "Spring 5 WebClient";
    private final WebClient webClient = WebClient.builder()
            .baseUrl(API_BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, API_MIME_TYPE)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build();


    // Obtener Listado
    @GetMapping
    public Flux<Person> clienteListado(){

        // pendiente de depurar
        return this.webClient.get()
                .uri("/api/persons")
                .exchangeToFlux(clientResponse -> {
                    System.out.println(clientResponse);
                    return clientResponse.bodyToFlux(Person.class);
                });
    }
    // Alta Objeto
    @PostMapping
    public Mono<ResponseEntity<Person>> clienteAlta(@RequestBody Person person){

        // pendiente de depurar
        return this.webClient.post()
                .uri("/api/persons")
                .body(Mono.just(person), Person.class)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }
    // Obtener Objeto por ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Person>> obtenClienteRemoto(@PathVariable(value = "id") String id){

        return this.webClient.get()
                .uri("/api/persons/"+id)
                .exchangeToMono(response -> {
                    Mono<ResponseEntity<Person>> person = response.toEntity(Person.class);
                    person.doOnNext(data -> {
                        // procesado asíncrono de datos
                        System.out.println(data);
                    });
                    return person ;
                });
    }
    // Modificar Objeto por ID
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Person>> modificaClienteRemoto(@PathVariable(value = "id") String id, @RequestBody Person person){

        return this.webClient.put()
                .uri("/api/persons/"+id)
                .body(Mono.just(person), Person.class)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }
    // Borrar Objeto por ID
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Person>> borrarClienteRemoto(@PathVariable(value = "id") String id){

        return this.webClient.delete()
                .uri("/api/persons/"+id)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }


}
