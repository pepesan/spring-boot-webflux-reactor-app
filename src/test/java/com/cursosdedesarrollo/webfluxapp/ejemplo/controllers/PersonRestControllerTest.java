package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRestControllerTest {

    @Mock
    private ReactivePersonRepository repo;

    @InjectMocks
    private PersonRestController controller;

    private WebTestClient client;
    private Person david;

    @BeforeEach
    void setUp() {
        david = new Person("1", "David", "Vaquero");
        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    @Test
    void getAll_returnsPersonsFromRepo() {
        when(repo.findAll()).thenReturn(Flux.just(david));

        client.get().uri("/api/persons")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getName()).isEqualTo("David"));
    }

    @Test
    void addPerson_savesAndReturns200() {
        when(repo.save(any(Person.class))).thenReturn(Mono.just(david));

        client.post().uri("/api/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(david)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> assertThat(p.getName()).isEqualTo("David"));
    }

    @Test
    void getById_found_returns200() {
        when(repo.findById("1")).thenReturn(Mono.just(david));

        client.get().uri("/api/persons/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> assertThat(p.getId()).isEqualTo("1"));
    }

    @Test
    void getById_notFound_returns404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.get().uri("/api/persons/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateById_found_returns200WithUpdatedPerson() {
        Person updated = new Person("1", "DAvid", "CowBoy");
        when(repo.findById("1")).thenReturn(Mono.just(david));
        when(repo.save(any(Person.class))).thenReturn(Mono.just(updated));

        client.put().uri("/api/persons/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updated)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assertThat(p.getName()).isEqualTo("DAvid");
                    assertThat(p.getLastName()).isEqualTo("CowBoy");
                });
    }

    @Test
    void updateById_notFound_returns404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.put().uri("/api/persons/NOPE")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(david)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_found_returns200WithDeletedPerson() {
        when(repo.findById("1")).thenReturn(Mono.just(david));
        when(repo.delete(david)).thenReturn(Mono.empty());

        client.delete().uri("/api/persons/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> assertThat(p.getId()).isEqualTo("1"));
    }

    @Test
    void deleteById_notFound_returns404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.delete().uri("/api/persons/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getByName_returnsMatchingPersons() {
        when(repo.findByName(eq("David"), any(PageRequest.class))).thenReturn(Flux.just(david));

        client.get().uri("/api/persons/byName/David")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1);
    }

    @Test
    void getByNameOrdered_returnsMatchingPersons() {
        when(repo.findByNameOrderByLastName(eq("David"), any(PageRequest.class))).thenReturn(Flux.just(david));

        client.get().uri("/api/persons/byNameResponseEntity/David")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1);
    }

    @Test
    void stream_emitePersonasCadaDosSegundos() {
        when(repo.findAll()).thenReturn(Flux.just(david));

        StepVerifier.withVirtualTime(() -> controller.streamAllPersons().take(2))
                .thenAwait(Duration.ofSeconds(2))
                .expectNext(david)
                .thenAwait(Duration.ofSeconds(2))
                .expectNext(david)
                .verifyComplete();
    }
}
