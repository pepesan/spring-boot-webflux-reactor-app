package com.cursosdedesarrollo.webfluxapp.ejercicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteRepository repo;

    @InjectMocks
    private ClienteController controller;

    private WebTestClient client;
    private Cliente empresa;

    @BeforeEach
    void setUp() {
        empresa = new Cliente();
        empresa.setId("1");
        empresa.setNombre("Empresa ABC");
        empresa.setDireccion("Calle Mayor 1");
        empresa.setTlf("600000000");
        empresa.setCorreo("abc@empresa.com");

        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    @Test
    void getAll_retornaClientesDelRepositorio() {
        when(repo.findAll()).thenReturn(Flux.just(empresa));

        client.get().uri("/api/clientes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Cliente.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getNombre()).isEqualTo("Empresa ABC"));
    }

    @Test
    void postOne_guardaYRetorna200() {
        when(repo.save(any(Cliente.class))).thenReturn(Mono.just(empresa));

        client.post().uri("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(empresa)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cliente.class)
                .value(c -> assertThat(c.getNombre()).isEqualTo("Empresa ABC"));
    }

    @Test
    void getById_encontrado_retorna200() {
        when(repo.findById("1")).thenReturn(Mono.just(empresa));

        client.get().uri("/api/clientes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cliente.class)
                .value(c -> assertThat(c.getId()).isEqualTo("1"));
    }

    @Test
    void getById_noEncontrado_retorna404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.get().uri("/api/clientes/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postById_encontrado_actualizaYRetorna200() {
        Cliente actualizado = new Cliente();
        actualizado.setId("1");
        actualizado.setNombre("Empresa ABC Actualizada");
        actualizado.setDireccion("Calle Mayor 2");

        when(repo.findById("1")).thenReturn(Mono.just(empresa));
        when(repo.save(any(Cliente.class))).thenReturn(Mono.just(actualizado));

        client.post().uri("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(actualizado)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cliente.class)
                .value(c -> {
                    assertThat(c.getNombre()).isEqualTo("Empresa ABC Actualizada");
                    assertThat(c.getDireccion()).isEqualTo("Calle Mayor 2");
                });
    }

    @Test
    void postById_noEncontrado_retorna404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.post().uri("/api/clientes/NOPE")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(empresa)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_encontrado_retorna200ConClienteEliminado() {
        when(repo.findById("1")).thenReturn(Mono.just(empresa));
        when(repo.delete(empresa)).thenReturn(Mono.empty());

        client.delete().uri("/api/clientes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cliente.class)
                .value(c -> assertThat(c.getId()).isEqualTo("1"));
    }

    @Test
    void deleteById_noEncontrado_retorna404() {
        when(repo.findById("NOPE")).thenReturn(Mono.empty());

        client.delete().uri("/api/clientes/NOPE")
                .exchange()
                .expectStatus().isNotFound();
    }
}
