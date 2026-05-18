package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

class MainControllerTest {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = MockMvcWebTestClient.bindToController(new MainController()).build();
    }

    @Test
    void getPerson_returns200WithDavid() {
        client.get().uri("/api/main")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assertThat(p.getId()).isEqualTo("1L");
                    assertThat(p.getName()).isEqualTo("David");
                    assertThat(p.getLastName()).isEqualTo("Vaquero");
                });
    }

    @Test
    void getPersons_returns200WithList() {
        client.get().uri("/api/main/list")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0][0].name").isEqualTo("David");
    }

    @Test
    void getPersonsIterable_returns200WithPersonList() {
        client.get().uri("/api/main/list-iterable")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getName()).isEqualTo("David");
                    assertThat(list.get(0).getId()).isEqualTo("1L");
                });
    }
}
