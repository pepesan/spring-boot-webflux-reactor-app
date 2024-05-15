package com.cursosdedesarrollo.webfluxapp;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringBootWebfluxReactiveMongoApplicationTests {
    private static final String SERVER_BASE_URL= "http://localhost:8080";

    private static final String BASE_URL = "/api/persons";

    @Autowired
    private ReactivePersonRepository personRepository;

    private WebTestClient webClient;
    @BeforeEach
    public void setUp() {
        this.webClient= WebTestClient
                .bindToServer()
                .baseUrl(SERVER_BASE_URL)
                .build();
    }


    @Test
    public void testObtenerListaPersonas() {
        byte[] resultado = webClient
                .get().uri(BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().returnResult().getResponseBody();
        System.out.format("%s: %s%n", "La lista de personas:",  new String(resultado));
    }

    @Test
    public void testCreatePerson() {
        Person person = new Person();
        person.setName("Pepe");

        webClient.post().uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(person), Person.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Pepe");
    }
    /*
    @Test
    public void testGetSinglePerson() {
        Person person= new Person();
        person.setName("Pepe");
        person=personRepository.save(person).block();

        webClient.get()
                .uri(BASE_URL+"/{id}", Collections.singletonMap("id", person.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.id").isEqualTo(person.getId())
                .jsonPath("$.name").isEqualTo("Pepe")
                .consumeWith(response ->{
                            Assertions.assertThat(response.getResponseBody()).isNotNull();
                        }
                        );
        personRepository.delete(person);
    }
    @Test
    public void testDeleteSinglePerson() {
        Person person= new Person();
        person.setName("Pepe");
        person=personRepository.save(person).block();

        webClient.delete()
                .uri(BASE_URL+"/{id}", Collections.singletonMap("id", person.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.id").isEqualTo(person.getId())
                .jsonPath("$.name").isEqualTo("Pepe")
                .consumeWith(response ->{
                            Assertions.assertThat(response.getResponseBody()).isNotNull();
                        }
                );
        Mono<Person>monoPerson=personRepository.findById(person.getId());
        monoPerson.subscribe(persona ->{
            Assertions.assertThat(persona).isNull();
        });
    }

    @Test
    public void testUpdatePerson() {
        Person person= new Person();
        person.setName("Pepe");
        person=personRepository.save(person).block();

        Person person2= new Person();
        person2.setName("Pepe2");

        webClient.put()
                .uri(BASE_URL+"/{id}", Collections.singletonMap("id", person.getId()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(person2), Person.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Pepe2");
        personRepository.deleteById(person.getId());
    }

     */
}
