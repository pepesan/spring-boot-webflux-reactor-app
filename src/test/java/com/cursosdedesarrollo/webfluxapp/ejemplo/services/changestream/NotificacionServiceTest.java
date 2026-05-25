package com.cursosdedesarrollo.webfluxapp.ejemplo.services.changestream;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream.Notificacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.changestream.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @InjectMocks
    private NotificacionService service;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = new Notificacion("n1", "Sistema actualizado", "Versión 2.0", "INFO",
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_retornaFluxDeNotificaciones() {
        when(notificacionRepository.findAll()).thenReturn(Flux.just(notificacion));

        StepVerifier.create(service.findAll())
                .assertNext(n -> assertThat(n.getId()).isEqualTo("n1"))
                .verifyComplete();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_guardaYRetornaNotificacion() {
        when(notificacionRepository.save(notificacion)).thenReturn(Mono.just(notificacion));

        StepVerifier.create(service.create(notificacion))
                .assertNext(n -> assertThat(n.getTitulo()).isEqualTo("Sistema actualizado"))
                .verifyComplete();
    }

    // ── stream ────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void stream_emiteBodyDeEventosNoNulos() {
        ChangeStreamEvent<Notificacion> evento = mock(ChangeStreamEvent.class);
        when(evento.getBody()).thenReturn(notificacion);

        when(reactiveMongoTemplate.changeStream(
                eq("notificaciones"), any(ChangeStreamOptions.class), eq(Notificacion.class)))
                .thenReturn(Flux.just(evento));

        StepVerifier.create(service.stream())
                .assertNext(n -> {
                    assertThat(n.getTitulo()).isEqualTo("Sistema actualizado");
                    assertThat(n.getTipo()).isEqualTo("INFO");
                })
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void stream_filtraEventosSinBody() {
        ChangeStreamEvent<Notificacion> eventoConBody = mock(ChangeStreamEvent.class);
        when(eventoConBody.getBody()).thenReturn(notificacion);

        ChangeStreamEvent<Notificacion> eventoSinBody = mock(ChangeStreamEvent.class);
        when(eventoSinBody.getBody()).thenReturn(null);

        when(reactiveMongoTemplate.changeStream(
                eq("notificaciones"), any(ChangeStreamOptions.class), eq(Notificacion.class)))
                .thenReturn(Flux.just(eventoConBody, eventoSinBody));

        StepVerifier.create(service.stream())
                .assertNext(n -> assertThat(n.getId()).isEqualTo("n1"))
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void stream_sinEventos_completaVacio() {
        when(reactiveMongoTemplate.changeStream(
                eq("notificaciones"), any(ChangeStreamOptions.class), eq(Notificacion.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.stream())
                .verifyComplete();
    }
}
