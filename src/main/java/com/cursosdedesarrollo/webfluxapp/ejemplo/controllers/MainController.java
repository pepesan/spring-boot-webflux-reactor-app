package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Main — Ejemplos Mono/Flux", description = "Ejemplos didácticos de retorno de Mono y Flux desde un controller. Sin base de datos.")
@Slf4j
@RestController
@RequestMapping("/api/main")
public class MainController {

    @Operation(summary = "Obtener una persona (Mono)", description = "Devuelve un único objeto Person envuelto en un Mono.")
    @ApiResponse(responseCode = "200", description = "Persona de ejemplo",
            content = @Content(schema = @Schema(implementation = Person.class)))
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Mono<Person> getPerson() {
        Person p =new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        return Mono.just(p);
    }
    @Operation(summary = "Lista de personas (Flux<List>)", description = "Devuelve una lista de personas envuelta en un Flux — ejemplo de Flux que emite listas.")
    @ApiResponse(responseCode = "200", description = "Lista de personas")
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Flux<List<Person>> getPersons() {
        Person p =new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        List<Person> listado = new ArrayList<>();
        listado.add(p);
        return Flux.just(listado);
    }
    @Operation(summary = "Personas como iterable (Flux<Person>)", description = "Devuelve personas como Flux<Person> construido desde un Iterable — ejemplo de Flux.fromIterable.")
    @ApiResponse(responseCode = "200", description = "Secuencia de personas",
            content = @Content(schema = @Schema(implementation = Person.class)))
    @GetMapping("/list-iterable")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Person> getPersonsIterable() {
        Person p = new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        List<Person> listado = new ArrayList<>();
        listado.add(p);
        return Flux.fromIterable(listado); // Convierte la lista en un Flux<Person>
    }
}
