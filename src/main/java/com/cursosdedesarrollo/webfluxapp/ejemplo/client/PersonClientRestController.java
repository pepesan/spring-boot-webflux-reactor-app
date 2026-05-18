package com.cursosdedesarrollo.webfluxapp.ejemplo.client;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Tag(name = "Persons (WebClient proxy)", description = "Proxy reactivo hacia /api/persons usando WebClient. Demuestra el patrón cliente HTTP reactivo entre servicios.")
@RestController
@RequestMapping("/api/persons/client")
public class PersonClientRestController {
    private static final String API_MIME_TYPE = "application/json";
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final String USER_AGENT = "Spring 5 WebClient";
    private final WebClient webClient = WebClient.builder()
            .baseUrl(API_BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, API_MIME_TYPE)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build();


    @Operation(summary = "Listar personas vía WebClient")
    @ApiResponse(responseCode = "200", description = "Lista de personas obtenida de /api/persons")
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
    @Operation(summary = "Crear persona vía WebClient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona creada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<Person>> clienteAlta(@RequestBody Person person){

        // pendiente de depurar
        return this.webClient.post()
                .uri("/api/persons")
                .body(Mono.just(person), Person.class)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }
    @Operation(summary = "Obtener persona por ID vía WebClient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona encontrada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Person>> obtenClienteRemoto(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id){

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
    @Operation(summary = "Actualizar persona por ID vía WebClient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona actualizada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Person>> modificaClienteRemoto(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id,
            @RequestBody Person person){

        return this.webClient.put()
                .uri("/api/persons/"+id)
                .body(Mono.just(person), Person.class)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }
    @Operation(summary = "Eliminar persona por ID vía WebClient")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona eliminada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Person>> borrarClienteRemoto(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id){

        return this.webClient.delete()
                .uri("/api/persons/"+id)
                .exchangeToMono(response -> response.toEntity(Person.class))
                .timeout(Duration.ofSeconds(5));
    }


}
