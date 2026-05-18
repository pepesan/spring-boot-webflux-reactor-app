package com.cursosdedesarrollo.webfluxapp.aceptacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.PersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Tag("Aceptance")
public class AceptacionReactivaTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        webTestClient.get()
                .uri("/api/persons-simple/clear")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getAllShouldReturnDavid() {
        webTestClient.get()
                .uri("/api/persons-simple")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .hasSize(1)
                .contains(new Person("1L", "David", "Vaquero"));
    }

    @Test
    public void testCrearPersona() {
        PersonDTO dto = new PersonDTO("Lorena", "Reyes");

        webTestClient.post()
                .uri("/api/persons-simple")
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
    public void testObtenerPorId() {
        webTestClient.get()
                .uri("/api/persons-simple/1L")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getName().equals("David");
                    assert p.getLastName().equals("Vaquero");
                    assert p.getId().equals("1L");
                });
    }

    @Test
    public void testActualizarPorId() {
        PersonDTO dto = new PersonDTO("DAvid", "CowBoy");

        webTestClient.put()
                .uri("/api/persons-simple/1L")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getName().equals("DAvid");
                    assert p.getLastName().equals("CowBoy");
                    assert p.getId().equals("1L");
                });
    }

    @Test
    public void testEliminarPorId() {
        webTestClient.delete()
                .uri("/api/persons-simple/1L")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .value(p -> {
                    assert p.getName().equals("David");
                    assert p.getLastName().equals("Vaquero");
                    assert p.getId().equals("1L");
                });
    }
}
