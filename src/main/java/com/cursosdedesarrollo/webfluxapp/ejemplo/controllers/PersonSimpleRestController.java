package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.PersonDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
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


@Slf4j
@RestController
@RequestMapping("/api/persons-simple")
public class PersonSimpleRestController {

    private List<Person> listado;
    private Integer counter;

    public PersonSimpleRestController(){
        this.listado = new ArrayList<>();
        Person p = new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        this.listado.add(p);
        this.counter = 1;
    }
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Flux<Person> getAllTweets() {
        return Flux.fromIterable(this.listado);
    }
    // método reactivo para el manejo de peticiones y la consulta a la BBDD
    @PostMapping
    private Mono<ResponseEntity<Person>> addPerson(@Valid @RequestBody PersonDTO person) {
        Person p = new Person(person);
        this.counter+=1;
        p.setId(this.counter+"L");
        this.listado.add(p);
        return Mono.just(ResponseEntity.ok(p));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Person>> getPersonById(@PathVariable(value = "id") String id) {
        return this.listado
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(person -> Mono.just(ResponseEntity.ok(person)))
                .orElseGet(() -> Mono.just(ResponseEntity.notFound().build()));

    }


    @PutMapping("/{id}")
    public Mono<ResponseEntity<Person>> updatePersonById(@PathVariable(value = "id") String id,
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
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Person>> deletePerson(@PathVariable(value = "id") String id) {

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
