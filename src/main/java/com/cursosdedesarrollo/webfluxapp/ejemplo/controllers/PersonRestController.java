package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
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
import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Tag(name = "Persons (MongoDB)", description = "CRUD completo de personas persistido en MongoDB reactivo.")
@Slf4j
@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private ReactivePersonRepository reactiveMongoRepository;

    @Autowired
    public PersonRestController(ReactivePersonRepository reactiveMongoRepository){
        this.reactiveMongoRepository = reactiveMongoRepository;
    }

    @Operation(summary = "Listar todas las personas")
    @ApiResponse(responseCode = "200", description = "Lista de personas")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Flux<Person> getAllPerson() {
        return reactiveMongoRepository.findAll();
    }
    @Operation(summary = "Crear una persona", description = "Persiste una nueva persona en MongoDB. name: 4–20 caracteres, no nulo. lastName: no nulo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona creada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    })
    @PostMapping
    private Mono<ResponseEntity<Person>> addPerson(@Valid @RequestBody Person person) {
        return reactiveMongoRepository.save(person)
                .map(data -> ResponseEntity.ok(data))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @Operation(summary = "Obtener persona por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona encontrada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Person>> getPersonById(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id) {
        return reactiveMongoRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @Operation(summary = "Actualizar persona por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona actualizada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Person>> updatePersonById(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id,
            @Valid @RequestBody Person person) {
        return reactiveMongoRepository.findById(id)
                .flatMap(existingPerson -> {
                    existingPerson.setName(person.getName());
                    existingPerson.setLastName(person.getLastName());
                    return reactiveMongoRepository.save(existingPerson);
                })
                .map(updatedPerson -> new ResponseEntity<>(updatedPerson, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @Operation(summary = "Eliminar persona por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Persona eliminada",
                content = @Content(schema = @Schema(implementation = Person.class))),
        @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Person>> deletePerson(
            @Parameter(description = "ID MongoDB de la persona") @PathVariable(value = "id") String id) {

        return reactiveMongoRepository.findById(id)
                .flatMap(person ->
                        reactiveMongoRepository.delete(person)
                                .then(
                                        Mono.just(
                                                new ResponseEntity<Person>(person, HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @Operation(summary = "Buscar por nombre (paginado)", description = "Devuelve personas cuyo nombre coincide exactamente. Devuelve [] si no hay coincidencias.")
    @ApiResponse(responseCode = "200", description = "Página de resultados")
    @GetMapping("/byName/{name}")
    private Flux<Person> getAllPersons(
            @Parameter(description = "Nombre a buscar") @PathVariable(value = "name") String name,
            @Parameter(description = "Número de página (0-based)") @RequestParam(name = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página") @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return reactiveMongoRepository.findByName(name, PageRequest.of(page, size));
    }
    @Operation(summary = "Buscar por nombre ordenado por apellido", description = "Igual que byName pero el resultado viene ordenado alfabéticamente por lastName.")
    @ApiResponse(responseCode = "200", description = "Página de resultados ordenada por apellido")
    @GetMapping("/byNameResponseEntity/{name}")
    @ResponseStatus(HttpStatus.OK)
    private Flux<Person> getAllPersonsResponseEntity(
            @Parameter(description = "Nombre a buscar") @PathVariable(value = "name") String name,
            @Parameter(description = "Número de página (0-based)") @RequestParam(name = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página") @RequestParam(name = "size", defaultValue = "10") Integer size) {
            return reactiveMongoRepository.findByNameOrderByLastName(name, PageRequest.of(page,size));
    }
    @Operation(summary = "Stream SSE de personas", description = "Consulta la BD cada 2 segundos y emite todas las personas como eventos SSE de forma continua. Consume con Accept: text/event-stream.")
    @ApiResponse(responseCode = "200", description = "Stream de personas",
            content = @Content(schema = @Schema(implementation = Person.class)))
    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<Person> streamAllPersons() {
        return Flux.interval(Duration.ofSeconds(2))
                   .flatMap(i -> reactiveMongoRepository.findAll());
    }


}
