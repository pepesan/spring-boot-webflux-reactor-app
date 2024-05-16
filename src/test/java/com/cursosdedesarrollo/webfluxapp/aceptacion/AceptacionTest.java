package com.cursosdedesarrollo.webfluxapp.aceptacion;


import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.PersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Tag("Aceptance")
public class AceptacionTest {

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private String apiURL;

    @BeforeEach
    public void setUp() {
        apiURL = "http://localhost:" + port ;
        this.restTemplate.getForObject(apiURL+"/api/persons-simple/clear", String.class);
    }

    @Test
    public void greetingShouldReturnDefaultMessage() throws Exception {
        assertThat(this.restTemplate.getForObject(apiURL+ "/api/persons-simple",
                String.class)).contains("David");
    }

    @Test
    public void testEnviarDatosAlApi() {
        // Crear un objeto MyObject con la cadena que deseas enviar
        PersonDTO p = new PersonDTO();
        p.setName("Lorena");
        p.setLastName("Reyes");

        // Configurar las cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Crear la entidad HTTP con el objeto y las cabeceras
        HttpEntity<PersonDTO> requestEntity = new HttpEntity<>(p, headers);

        // Crear un objeto RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Realizar la solicitud POST al API REST
        ResponseEntity<Person> responseEntity = restTemplate.exchange(
                this.apiURL+ "/api/persons-simple",
                HttpMethod.POST,
                requestEntity,
                Person.class
        );

        // Verificar que la respuesta del servidor sea exitosa (código 2xx)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Puedes verificar más cosas según tus necesidades, por ejemplo, el contenido de la respuesta
        Person responseBody = responseEntity.getBody();
        assertEquals("Lorena", responseBody.getName());
        assertEquals("Reyes", responseBody.getLastName());
        assertEquals("2L", responseBody.getId());
    }

    @Test
    public void testShowByIdApiWebFlux() {
        // Configurar las cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Crear la entidad HTTP con el objeto y las cabeceras
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        // Crear un objeto RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Realizar la solicitud POST al API REST
        ResponseEntity<Person> responseEntity = restTemplate.exchange(
                this.apiURL+ "/api/persons-simple/1L",
                HttpMethod.GET,
                requestEntity,
                Person.class
        );

        // Verificar que la respuesta del servidor sea exitosa (código 2xx)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Puedes verificar más cosas según tus necesidades, por ejemplo, el contenido de la respuesta
        Person responseBody = responseEntity.getBody();
        assertEquals("David", responseBody.getName());
        assertEquals("Vaquero", responseBody.getLastName());
        assertEquals("1L", responseBody.getId());
    }
    @Test
    public void testUpdateByIdApiWebFlux() {
        PersonDTO p = new PersonDTO();
        p.setName("DAvid");
        p.setLastName("CowBoy");
        // Configurar las cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Crear la entidad HTTP con el objeto y las cabeceras
        HttpEntity<PersonDTO> requestEntity = new HttpEntity<>(p, headers);

        // Crear un objeto RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Realizar la solicitud POST al API REST
        ResponseEntity<Person> responseEntity = restTemplate.exchange(
                this.apiURL+ "/api/persons-simple/1L",
                HttpMethod.PUT,
                requestEntity,
                Person.class
        );

        // Verificar que la respuesta del servidor sea exitosa (código 2xx)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Puedes verificar más cosas según tus necesidades, por ejemplo, el contenido de la respuesta
        Person responseBody = responseEntity.getBody();
        assertEquals("DAvid", responseBody.getName());
        assertEquals("CowBoy", responseBody.getLastName());
        assertEquals("1L", responseBody.getId());
    }

    @Test
    public void testDeleteByIdApiWebFlux() {
        // Configurar las cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Crear la entidad HTTP con el objeto y las cabeceras
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        // Crear un objeto RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Realizar la solicitud POST al API REST
        ResponseEntity<Person> responseEntity = restTemplate.exchange(
                this.apiURL+ "/api/persons-simple/1L",
                HttpMethod.DELETE,
                requestEntity,
                Person.class
        );

        // Verificar que la respuesta del servidor sea exitosa (código 2xx)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Puedes verificar más cosas según tus necesidades, por ejemplo, el contenido de la respuesta
        Person responseBody = responseEntity.getBody();
        assertEquals("David", responseBody.getName());
        assertEquals("Vaquero", responseBody.getLastName());
        assertEquals("1L", responseBody.getId());
    }

}