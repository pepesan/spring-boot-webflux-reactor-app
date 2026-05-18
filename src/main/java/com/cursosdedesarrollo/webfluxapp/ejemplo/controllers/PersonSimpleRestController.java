package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.PersonDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name = "Persons (Memoria)", description = "CRUD de personas en memoria. Sin base de datos — útil para probar comportamiento reactivo sin infraestructura.")
@Slf4j
@RestController
@RequestMapping("/api/persons-simple")
public class PersonSimpleRestController {

    private List<Person> listado;
    private Integer counter;

    public PersonSimpleRestController(){
        resetData();
    }

    private void resetData() {
        this.listado = new ArrayList<>();
        Person p = new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        this.listado.add(p);
        this.counter = 1;
    }

    @Operation(summary = "Listar todas las personas", description = "Devuelve la lista completa de personas almacenadas en memoria.")
    @ApiResponse(responseCode = "200", description = "Lista de personas")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Flux<Person> getAllTweets() {
        return Flux.fromIterable(this.listado);
    }
    @Operation(summary = "Crear una persona", description = "Añade una nueva persona a la lista en memoria. name: 4–20 caracteres, no nulo. lastName: no nulo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona creada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    })
    @PostMapping
    private Mono<ResponseEntity<Person>> addPerson(@Valid @RequestBody PersonDTO person) {
        Person p = new Person(person);
        this.counter+=1;
        p.setId(this.counter+"L");
        this.listado.add(p);
        return Mono.just(ResponseEntity.ok(p));
    }

    @Operation(summary = "Resetear datos", description = "Elimina todas las personas y restaura el estado inicial (una persona de ejemplo).")
    @ApiResponse(responseCode = "200", description = "Datos reseteados")
    @GetMapping("/clear")
    public Mono<String> clearData(){
        this.resetData();
        return Mono.just("OK");
    }

    @Operation(summary = "Obtener persona por ID", description = "Busca una persona en memoria por su identificador.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona encontrada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Person>> getPersonById(
            @Parameter(description = "Identificador de la persona (p.ej. 1L)") @PathVariable(value = "id") String id) {
        return this.listado
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(person -> Mono.just(ResponseEntity.ok(person)))
                .orElseGet(() -> Mono.just(ResponseEntity.notFound().build()));

    }


    @Operation(summary = "Actualizar persona por ID", description = "Sustituye los datos de una persona en memoria.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona actualizada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Person>> updatePersonById(
            @Parameter(description = "Identificador de la persona") @PathVariable(value = "id") String id,
            @Valid @RequestBody PersonDTO person) {
        return this.listado
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(personaAlmacenada -> {
                    // substituir el elemento del listado por otro
                    this.listado.remove(personaAlmacenada);
                    Person p = new Person(person);
                    p.setId(id);
                    this.listado.add(p);
                    return Mono.just(ResponseEntity.ok(p));
                })
                .orElseGet(() -> Mono.just(ResponseEntity.notFound().build()));
    }
    @Operation(summary = "Eliminar persona por ID", description = "Elimina una persona de la lista en memoria y la devuelve.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona eliminada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Person>> deletePerson(
            @Parameter(description = "Identificador de la persona") @PathVariable(value = "id") String id) {

        return this.listado
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(personaAlmacenada -> {
                    // substituir el elemento del listado por otro
                    this.listado.remove(personaAlmacenada);
                    return Mono.just(ResponseEntity.ok(personaAlmacenada));
                })
                .orElseGet(() -> Mono.just(ResponseEntity.notFound().build()));
    }
}
