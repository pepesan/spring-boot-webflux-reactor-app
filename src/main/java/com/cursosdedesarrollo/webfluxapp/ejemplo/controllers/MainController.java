package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
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

@Slf4j
@RestController
@RequestMapping("/api/main")
public class MainController {
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Mono<Person> getPerson() {
        Person p =new Person();
        p.setId("1L");
        p.setName("David");
        p.setLastName("Vaquero");
        return Mono.just(p);
    }
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
