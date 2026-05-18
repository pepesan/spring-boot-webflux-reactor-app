package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.EstudianteConMatriculasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.MatriculacionConCursoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.naem.MatriculacionService;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class EstudianteControllerTest {

    @Mock
    private MatriculacionService matriculacionService;

    @InjectMocks
    private EstudianteController controller;

    private WebTestClient client;
    private Estudiante estudiante;
    private Curso curso;
    private Matriculacion matriculacion;

    @BeforeEach
    void setUp() {
        estudiante = new Estudiante("e1", "Laura Martínez", "laura@ejemplo.com");
        curso = new Curso("c1", "Spring WebFlux", "Programación reactiva", 30);
        matriculacion = new Matriculacion("m1", "e1", "c1", LocalDate.of(2024, 9, 1), null);

        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    // ── Estudiantes ──────────────────────────────────────────────────────────

    @Test
    void getAllEstudiantes_retornaLista() {
        when(matriculacionService.findAllEstudiantes()).thenReturn(Flux.just(estudiante));

        client.get().uri("/api/relaciones/nm/estudiantes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Estudiante.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getNombre()).isEqualTo("Laura Martínez"));
    }

    @Test
    void createEstudiante_retorna201() {
        when(matriculacionService.createEstudiante(any(Estudiante.class))).thenReturn(Mono.just(estudiante));

        client.post().uri("/api/relaciones/nm/estudiantes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Laura Martínez\",\"email\":\"laura@ejemplo.com\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Estudiante.class)
                .value(e -> assertThat(e.getEmail()).isEqualTo("laura@ejemplo.com"));
    }

    @Test
    void getCursosDeEstudiante_retornaCursos() {
        when(matriculacionService.findCursosDeEstudiante("e1")).thenReturn(Flux.just(curso));

        client.get().uri("/api/relaciones/nm/estudiantes/e1/cursos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Curso.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getTitulo()).isEqualTo("Spring WebFlux"));
    }

    @Test
    void getCursosDeEstudiante_noEncontrado_retorna404() {
        when(matriculacionService.findCursosDeEstudiante("nope"))
                .thenReturn(Flux.error(new ResponseStatusException(NOT_FOUND, "Estudiante no encontrado")));

        client.get().uri("/api/relaciones/nm/estudiantes/nope/cursos")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getEstudianteConMatriculas_retornaDTOCompleto() {
        MatriculacionConCursoDTO mDto = new MatriculacionConCursoDTO(matriculacion, curso);
        EstudianteConMatriculasDTO dto = new EstudianteConMatriculasDTO(estudiante, List.of(mDto));
        when(matriculacionService.findEstudianteConMatriculas("e1")).thenReturn(Mono.just(dto));

        client.get().uri("/api/relaciones/nm/estudiantes/e1/completo")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EstudianteConMatriculasDTO.class)
                .value(d -> {
                    assertThat(d.estudiante().getId()).isEqualTo("e1");
                    assertThat(d.matriculas()).hasSize(1);
                    assertThat(d.matriculas().get(0).curso().getTitulo()).isEqualTo("Spring WebFlux");
                });
    }

    @Test
    void getEstudianteConMatriculas_noEncontrado_retorna404() {
        when(matriculacionService.findEstudianteConMatriculas("nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Estudiante no encontrado")));

        client.get().uri("/api/relaciones/nm/estudiantes/nope/completo")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Cursos ───────────────────────────────────────────────────────────────

    @Test
    void getAllCursos_retornaLista() {
        when(matriculacionService.findAllCursos()).thenReturn(Flux.just(curso));

        client.get().uri("/api/relaciones/nm/cursos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Curso.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getPlazas()).isEqualTo(30));
    }

    @Test
    void createCurso_retorna201() {
        when(matriculacionService.createCurso(any(Curso.class))).thenReturn(Mono.just(curso));

        client.post().uri("/api/relaciones/nm/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"titulo\":\"Spring WebFlux\",\"plazas\":30}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Curso.class)
                .value(c -> assertThat(c.getTitulo()).isEqualTo("Spring WebFlux"));
    }

    @Test
    void getEstudiantesDeCurso_retornaEstudiantes() {
        when(matriculacionService.findEstudiantesDeCurso("c1")).thenReturn(Flux.just(estudiante));

        client.get().uri("/api/relaciones/nm/cursos/c1/estudiantes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Estudiante.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getNombre()).isEqualTo("Laura Martínez"));
    }

    @Test
    void getEstudiantesDeCurso_noEncontrado_retorna404() {
        when(matriculacionService.findEstudiantesDeCurso("nope"))
                .thenReturn(Flux.error(new ResponseStatusException(NOT_FOUND, "Curso no encontrado")));

        client.get().uri("/api/relaciones/nm/cursos/nope/estudiantes")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Matrículas ───────────────────────────────────────────────────────────

    @Test
    void matricular_retorna201() {
        when(matriculacionService.matricular("e1", "c1")).thenReturn(Mono.just(matriculacion));

        client.post().uri("/api/relaciones/nm/matriculaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estudianteId\":\"e1\",\"cursoId\":\"c1\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Matriculacion.class)
                .value(m -> {
                    assertThat(m.getEstudianteId()).isEqualTo("e1");
                    assertThat(m.getCursoId()).isEqualTo("c1");
                });
    }

    @Test
    void matricular_estudianteNoExiste_retorna404() {
        when(matriculacionService.matricular("nope", "c1"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Estudiante no encontrado")));

        client.post().uri("/api/relaciones/nm/matriculaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estudianteId\":\"nope\",\"cursoId\":\"c1\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void matricular_yaMatriculado_retorna409() {
        when(matriculacionService.matricular("e1", "c1"))
                .thenReturn(Mono.error(new ResponseStatusException(CONFLICT, "El estudiante ya está matriculado en este curso")));

        client.post().uri("/api/relaciones/nm/matriculaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"estudianteId\":\"e1\",\"cursoId\":\"c1\"}")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void desmatricular_encontrado_retorna200() {
        when(matriculacionService.desmatricular("e1", "c1"))
                .thenReturn(Mono.just(ResponseEntity.ok(matriculacion)));

        client.delete().uri("/api/relaciones/nm/matriculaciones/e1/c1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Matriculacion.class)
                .value(m -> assertThat(m.getId()).isEqualTo("m1"));
    }

    @Test
    void desmatricular_noEncontrado_retorna404() {
        when(matriculacionService.desmatricular("e1", "nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Matrícula no encontrada")));

        client.delete().uri("/api/relaciones/nm/matriculaciones/e1/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void actualizarNota_retornaMatriculaActualizada() {
        Matriculacion conNota = new Matriculacion("m1", "e1", "c1", matriculacion.getFechaAlta(), 9.0);
        when(matriculacionService.actualizarNota("e1", "c1", 9.0)).thenReturn(Mono.just(conNota));

        client.put().uri("/api/relaciones/nm/matriculaciones/e1/c1/nota?nota=9.0")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Matriculacion.class)
                .value(m -> assertThat(m.getNota()).isEqualTo(9.0));
    }

    @Test
    void actualizarNota_noEncontrado_retorna404() {
        when(matriculacionService.actualizarNota("e1", "nope", 5.0))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Matrícula no encontrada")));

        client.put().uri("/api/relaciones/nm/matriculaciones/e1/nope/nota?nota=5.0")
                .exchange()
                .expectStatus().isNotFound();
    }
}
