package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.PersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

class PersonSimpleRestControllerTest {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        // nueva instancia por test para garantizar estado limpio
        client = MockMvcWebTestClient.bindToController(new PersonSimpleRestController()).build();
    }

    @Test
    void getAll_returns200WithDavid() {
        client.get().uri("/api/persons-simple")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getName()).isEqualTo("David"));
    }

    @Test
    void clear_resetsDataAndReturnsOk() {
        client.get().uri("/api/persons-simple/clear")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("OK");
    }

    @Test
    void addPerson_returns200WithNewPerson() {
        PersonDTO dto = new PersonDTO("Lorena", "Reyes");

        client.post().uri("/api/persons-simple")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getName().equals("Lorena");
                    assert p.getLastName().equals("Reyes");
                    assert p.getId().equals("2L");
                });
    }

    @Test
    void getById_existingId_returns200() {
        client.get().uri("/api/persons-simple/1L")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getId().equals("1L");
                    assert p.getName().equals("David");
                });
    }

    @Test
    void getById_unknownId_returns404() {
        client.get().uri("/api/persons-simple/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateById_existingId_returns200WithUpdatedData() {
        PersonDTO dto = new PersonDTO("DAvid", "CowBoy");

        client.put().uri("/api/persons-simple/1L")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getId().equals("1L");
                    assert p.getName().equals("DAvid");
                    assert p.getLastName().equals("CowBoy");
                });
    }

    @Test
    void updateById_unknownId_returns404() {
        PersonDTO dto = new PersonDTO("DAvid", "CowBoy");

        client.put().uri("/api/persons-simple/NOPE")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_existingId_returns200WithDeletedPerson() {
        client.delete().uri("/api/persons-simple/1L")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getId().equals("1L");
                    assert p.getName().equals("David");
                });
    }

    @Test
    void deleteById_unknownId_returns404() {
        client.delete().uri("/api/persons-simple/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }
}
