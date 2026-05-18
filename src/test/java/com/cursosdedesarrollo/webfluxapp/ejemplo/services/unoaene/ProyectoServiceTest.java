package com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene.ProyectoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private ProyectoService service;

    private Proyecto proyecto;
    private Tarea tareaA;
    private Tarea tareaB;

    @BeforeEach
    void setUp() {
        proyecto = new Proyecto("p1", "Portal de clientes", "Descripción");
        tareaA = new Tarea("t1", "Diseñar login", "PENDIENTE", "p1");
        tareaB = new Tarea("t2", "Implementar API", "EN_CURSO", "p1");
    }

    // ── findTareasByProyecto (flatMapMany) ────────────────────────────────────

    @Test
    void findTareasByProyecto_emiteTarasConFlatMapMany() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoId("p1")).thenReturn(Flux.just(tareaA, tareaB));

        StepVerifier.create(service.findTareasByProyecto("p1"))
                .expectNext(tareaA)
                .expectNext(tareaB)
                .verifyComplete();
    }

    @Test
    void findTareasByProyecto_proyectoSinTareas_completaSinEmitir() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoId("p1")).thenReturn(Flux.empty());

        StepVerifier.create(service.findTareasByProyecto("p1"))
                .verifyComplete();
    }

    @Test
    void findTareasByProyecto_proyectoNoExiste_lanza404() {
        when(proyectoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findTareasByProyecto("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── findTareasByProyectoAndEstado (query derivada) ────────────────────────

    @Test
    void findTareasByProyectoAndEstado_filtraEnMongoDB() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoIdAndEstado("p1", "PENDIENTE")).thenReturn(Flux.just(tareaA));

        StepVerifier.create(service.findTareasByProyectoAndEstado("p1", "PENDIENTE"))
                .assertNext(t -> assertThat(t.getEstado()).isEqualTo("PENDIENTE"))
                .verifyComplete();
    }

    // ── findProyectoConTareas (collectList) ───────────────────────────────────

    @Test
    void findProyectoConTareas_agregaTareasEnDTO() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoId("p1")).thenReturn(Flux.just(tareaA, tareaB));

        StepVerifier.create(service.findProyectoConTareas("p1"))
                .assertNext(dto -> {
                    assertThat(dto.proyecto()).isEqualTo(proyecto);
                    assertThat(dto.tareas()).hasSize(2).contains(tareaA, tareaB);
                })
                .verifyComplete();
    }

    @Test
    void findProyectoConTareas_proyectoSinTareas_devuelveListaVacia() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoId("p1")).thenReturn(Flux.empty());

        StepVerifier.create(service.findProyectoConTareas("p1"))
                .assertNext(dto -> assertThat(dto.tareas()).isEmpty())
                .verifyComplete();
    }

    @Test
    void findProyectoConTareas_proyectoNoExiste_lanza404() {
        when(proyectoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findProyectoConTareas("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── deleteConTareas (cascada) ─────────────────────────────────────────────

    @Test
    void deleteConTareas_eliminaTareasYLuegoProyecto() {
        when(proyectoRepository.findById("p1")).thenReturn(Mono.just(proyecto));
        when(tareaRepository.findByProyectoId("p1")).thenReturn(Flux.just(tareaA, tareaB));
        when(tareaRepository.delete(tareaA)).thenReturn(Mono.empty());
        when(tareaRepository.delete(tareaB)).thenReturn(Mono.empty());
        when(proyectoRepository.delete(proyecto)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteConTareas("p1"))
                .assertNext(re -> assertThat(re.getBody()).isEqualTo(proyecto))
                .verifyComplete();
    }

    @Test
    void deleteConTareas_proyectoNoExiste_lanza404() {
        when(proyectoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteConTareas("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── createTarea ───────────────────────────────────────────────────────────

    @Test
    void createTarea_proyectoExiste_guardaLaTarea() {
        when(proyectoRepository.existsById("p1")).thenReturn(Mono.just(true));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(Mono.just(tareaA));

        StepVerifier.create(service.createTarea(tareaA))
                .expectNext(tareaA)
                .verifyComplete();
    }

    @Test
    void createTarea_proyectoNoExiste_lanza404() {
        when(proyectoRepository.existsById("nope")).thenReturn(Mono.just(false));
        tareaA.setProyectoId("nope");

        StepVerifier.create(service.createTarea(tareaA))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── updateEstado ──────────────────────────────────────────────────────────

    @Test
    void updateEstado_tareaExiste_actualizaElEstado() {
        when(tareaRepository.findById("t1")).thenReturn(Mono.just(tareaA));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.updateEstado("t1", "HECHO"))
                .assertNext(re -> assertThat(re.getBody().getEstado()).isEqualTo("HECHO"))
                .verifyComplete();
    }

    @Test
    void updateEstado_tareaNoExiste_lanza404() {
        when(tareaRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateEstado("nope", "HECHO"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }
}
