package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.changestream;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream.Notificacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.changestream.NotificacionService;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController controller;

    private WebTestClient client;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = new Notificacion("n1", "Sistema actualizado", "Versión 2.0", "INFO",
                LocalDateTime.of(2024, 1, 15, 10, 0));
        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    @Test
    void getAllNotificaciones_retornaLista() {
        when(notificacionService.findAll()).thenReturn(Flux.just(notificacion));

        client.get().uri("/api/changestream/notificaciones")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Notificacion.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getTitulo()).isEqualTo("Sistema actualizado"));
    }

    @Test
    void getAllNotificaciones_sinDatos_retornaListaVacia() {
        when(notificacionService.findAll()).thenReturn(Flux.empty());

        client.get().uri("/api/changestream/notificaciones")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Notificacion.class)
                .hasSize(0);
    }

    @Test
    void createNotificacion_retorna201() {
        when(notificacionService.create(any(Notificacion.class))).thenReturn(Mono.just(notificacion));

        client.post().uri("/api/changestream/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"titulo\":\"Sistema actualizado\",\"mensaje\":\"Versión 2.0\",\"tipo\":\"INFO\",\"fecha\":\"2024-01-15T10:00:00\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Notificacion.class)
                .value(n -> {
                    assertThat(n.getId()).isEqualTo("n1");
                    assertThat(n.getTipo()).isEqualTo("INFO");
                });
    }

    @Test
    void createNotificacion_datosInvalidos_retorna400() {
        client.post().uri("/api/changestream/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"titulo\":\"\",\"mensaje\":\"Mensaje\",\"tipo\":\"INFO\",\"fecha\":\"2024-01-15T10:00:00\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void streamNotificaciones_emiteDosEventos() {
        Notificacion segunda = new Notificacion("n2", "Error crítico", "BD inaccesible", "ERROR",
                LocalDateTime.of(2024, 1, 15, 11, 0));
        when(notificacionService.stream()).thenReturn(Flux.just(notificacion, segunda));

        client.get().uri("/api/changestream/notificaciones/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Notificacion.class)
                .hasSize(2)
                .value(list -> {
                    assertThat(list.get(0).getTipo()).isEqualTo("INFO");
                    assertThat(list.get(1).getTipo()).isEqualTo("ERROR");
                });
    }

    @Test
    void streamNotificaciones_fluxVacio_completaSinEmitir() {
        when(notificacionService.stream()).thenReturn(Flux.empty());

        client.get().uri("/api/changestream/notificaciones/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Notificacion.class)
                .hasSize(0);
    }
}
