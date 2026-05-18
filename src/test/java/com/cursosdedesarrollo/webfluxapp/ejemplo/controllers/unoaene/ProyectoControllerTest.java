package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoaene.ProyectoConTareasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoaene.ProyectoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class ProyectoControllerTest {

    @Mock
    private ProyectoService proyectoService;

    @InjectMocks
    private ProyectoController controller;

    private WebTestClient client;
    private Proyecto proyecto;
    private Tarea tareaA;
    private Tarea tareaB;

    @BeforeEach
    void setUp() {
        proyecto = new Proyecto("p1", "Portal de clientes", "Descripción");
        tareaA = new Tarea("t1", "Diseñar login", "PENDIENTE", "p1");
        tareaB = new Tarea("t2", "Implementar API", "EN_CURSO", "p1");

        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    // ── Proyectos ────────────────────────────────────────────────────────────

    @Test
    void getAllProyectos_retornaLista() {
        when(proyectoService.findAll()).thenReturn(Flux.just(proyecto));

        client.get().uri("/api/relaciones/1an/proyectos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Proyecto.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getNombre()).isEqualTo("Portal de clientes"));
    }

    @Test
    void createProyecto_retorna201() {
        when(proyectoService.create(any(Proyecto.class))).thenReturn(Mono.just(proyecto));

        client.post().uri("/api/relaciones/1an/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Portal de clientes\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Proyecto.class)
                .value(p -> assertThat(p.getNombre()).isEqualTo("Portal de clientes"));
    }

    @Test
    void getTareasByProyecto_sinFiltro_retornaTodasLasTareas() {
        when(proyectoService.findTareasByProyecto("p1")).thenReturn(Flux.just(tareaA, tareaB));

        client.get().uri("/api/relaciones/1an/proyectos/p1/tareas")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Tarea.class)
                .hasSize(2);
    }

    @Test
    void getTareasByProyecto_conFiltroEstado_delegaEnQueryDerivada() {
        when(proyectoService.findTareasByProyectoAndEstado("p1", "PENDIENTE"))
                .thenReturn(Flux.just(tareaA));

        client.get().uri("/api/relaciones/1an/proyectos/p1/tareas?estado=PENDIENTE")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Tarea.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getEstado()).isEqualTo("PENDIENTE"));
    }

    @Test
    void getProyectoConTareas_retornaDTOCompleto() {
        ProyectoConTareasDTO dto = new ProyectoConTareasDTO(proyecto, List.of(tareaA, tareaB));
        when(proyectoService.findProyectoConTareas("p1")).thenReturn(Mono.just(dto));

        client.get().uri("/api/relaciones/1an/proyectos/p1/completo")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProyectoConTareasDTO.class)
                .value(d -> {
                    assertThat(d.proyecto().getId()).isEqualTo("p1");
                    assertThat(d.tareas()).hasSize(2);
                });
    }

    @Test
    void getProyectoConTareas_noEncontrado_retorna404() {
        when(proyectoService.findProyectoConTareas("nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Proyecto no encontrado")));

        client.get().uri("/api/relaciones/1an/proyectos/nope/completo")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteProyecto_encontrado_retorna200ConProyecto() {
        when(proyectoService.deleteConTareas("p1"))
                .thenReturn(Mono.just(ResponseEntity.ok(proyecto)));

        client.delete().uri("/api/relaciones/1an/proyectos/p1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Proyecto.class)
                .value(p -> assertThat(p.getId()).isEqualTo("p1"));
    }

    @Test
    void deleteProyecto_noEncontrado_retorna404() {
        when(proyectoService.deleteConTareas("nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Proyecto no encontrado")));

        client.delete().uri("/api/relaciones/1an/proyectos/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Tareas ───────────────────────────────────────────────────────────────

    @Test
    void createTarea_retorna201() {
        when(proyectoService.createTarea(any(Tarea.class))).thenReturn(Mono.just(tareaA));

        client.post().uri("/api/relaciones/1an/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"titulo\":\"Diseñar login\",\"estado\":\"PENDIENTE\",\"proyectoId\":\"p1\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Tarea.class)
                .value(t -> assertThat(t.getTitulo()).isEqualTo("Diseñar login"));
    }

    @Test
    void updateEstado_retornaTareaActualizada() {
        Tarea actualizada = new Tarea("t1", "Diseñar login", "HECHO", "p1");
        when(proyectoService.updateEstado("t1", "HECHO"))
                .thenReturn(Mono.just(ResponseEntity.ok(actualizada)));

        client.put().uri("/api/relaciones/1an/tareas/t1/estado?estado=HECHO")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Tarea.class)
                .value(t -> assertThat(t.getEstado()).isEqualTo("HECHO"));
    }

    @Test
    void deleteTarea_encontrada_retorna200() {
        when(proyectoService.deleteTarea("t1"))
                .thenReturn(Mono.just(ResponseEntity.ok(tareaA)));

        client.delete().uri("/api/relaciones/1an/tareas/t1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Tarea.class)
                .value(t -> assertThat(t.getId()).isEqualTo("t1"));
    }

    @Test
    void deleteTarea_noEncontrada_retorna404() {
        when(proyectoService.deleteTarea("nope"))
                .thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.delete().uri("/api/relaciones/1an/tareas/nope")
                .exchange()
                .expectStatus().isNotFound();
    }
}
