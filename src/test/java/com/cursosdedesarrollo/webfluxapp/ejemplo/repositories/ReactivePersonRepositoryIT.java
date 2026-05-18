package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
class ReactivePersonRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    ReactivePersonRepository repository;

    @BeforeEach
    void limpiarBBDD() {
        repository.deleteAll().block();
    }

    // ── CRUD básico ──────────────────────────────────────────────────────────

    @Test
    void saveAndFindById_persisteYRecuperaCorrectamente() {
        Person person = new Person(null, "David", "Vaquero");

        repository.save(person)
                .flatMap(saved -> repository.findById(saved.getId()))
                .as(StepVerifier::create)
                .expectNextMatches(p ->
                        p.getName().equals("David") &&
                        p.getLastName().equals("Vaquero") &&
                        p.getId() != null)
                .verifyComplete();
    }

    @Test
    void findAll_devuelveTodosLosGuardados() {
        List<Person> personas = List.of(
                new Person(null, "David", "Vaquero"),
                new Person(null, "Maria", "Lopez"),
                new Person(null, "Carlos", "Garcia")
        );

        repository.saveAll(personas)
                .thenMany(repository.findAll())
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void delete_eliminaLaPersonaDeLaBBDD() {
        repository.save(new Person(null, "David", "Vaquero"))
                .flatMap(saved ->
                        repository.delete(saved)
                                .then(repository.findById(saved.getId())))
                .as(StepVerifier::create)
                .verifyComplete(); // empty — no encontrado tras borrar
    }

    // ── Queries derivadas ────────────────────────────────────────────────────

    @Test
    void findByName_devuelveSoloLasPersonasConEseNombre() {
        List<Person> personas = List.of(
                new Person(null, "David", "Vaquero"),
                new Person(null, "David", "Garcia"),
                new Person(null, "Maria", "Lopez")
        );

        repository.saveAll(personas)
                .thenMany(repository.findByName("David", PageRequest.of(0, 10)))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByName_conPaginacion_respetaTamanioDePagina() {
        List<Person> personas = List.of(
                new Person(null, "David", "A"),
                new Person(null, "David", "B"),
                new Person(null, "David", "C"),
                new Person(null, "David", "D"),
                new Person(null, "David", "E")
        );

        repository.saveAll(personas)
                .thenMany(repository.findByName("David", PageRequest.of(0, 2)))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByName_sinCoincidencias_devuelveFluxVacio() {
        repository.save(new Person(null, "David", "Vaquero"))
                .thenMany(repository.findByName("Inexistente", PageRequest.of(0, 10)))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByNameOrderByLastName_devuelveResultadosOrdenadosAlfabeticamente() {
        List<Person> personas = List.of(
                new Person(null, "David", "Zaragoza"),
                new Person(null, "David", "Alvarez"),
                new Person(null, "David", "Martin")
        );

        repository.saveAll(personas)
                .thenMany(repository.findByNameOrderByLastName("David", PageRequest.of(0, 10)))
                .as(StepVerifier::create)
                .expectNextMatches(p -> p.getLastName().equals("Alvarez"))
                .expectNextMatches(p -> p.getLastName().equals("Martin"))
                .expectNextMatches(p -> p.getLastName().equals("Zaragoza"))
                .verifyComplete();
    }

    @Test
    void findByNameOrderByLastName_conPaginacion_respetaOrdenYTamanio() {
        List<Person> personas = List.of(
                new Person(null, "David", "Zaragoza"),
                new Person(null, "David", "Alvarez"),
                new Person(null, "David", "Martin"),
                new Person(null, "David", "Blanco")
        );

        repository.saveAll(personas)
                .thenMany(repository.findByNameOrderByLastName("David", PageRequest.of(0, 2)))
                .as(StepVerifier::create)
                .expectNextMatches(p -> p.getLastName().equals("Alvarez"))
                .expectNextMatches(p -> p.getLastName().equals("Blanco"))
                .verifyComplete();
    }
}
