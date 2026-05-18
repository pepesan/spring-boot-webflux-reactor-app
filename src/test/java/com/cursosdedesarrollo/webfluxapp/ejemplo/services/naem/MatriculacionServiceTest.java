package com.cursosdedesarrollo.webfluxapp.ejemplo.services.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.EstudianteConMatriculasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.CursoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.EstudianteRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.MatriculacionRepository;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatriculacionServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private CursoRepository cursoRepository;
    @Mock
    private MatriculacionRepository matriculacionRepository;

    @InjectMocks
    private MatriculacionService service;

    private Estudiante estudiante;
    private Curso curso;
    private Matriculacion matriculacion;

    @BeforeEach
    void setUp() {
        estudiante = new Estudiante("e1", "Laura Martínez", "laura@ejemplo.com");
        curso = new Curso("c1", "Spring WebFlux", "Programación reactiva", 30);
        matriculacion = new Matriculacion("m1", "e1", "c1", LocalDate.of(2024, 9, 1), null);
    }

    // ── CRUD Estudiante ──────────────────────────────────────────────────────

    @Test
    void findAllEstudiantes_retornaFlux() {
        when(estudianteRepository.findAll()).thenReturn(Flux.just(estudiante));

        StepVerifier.create(service.findAllEstudiantes())
                .expectNext(estudiante)
                .verifyComplete();
    }

    @Test
    void createEstudiante_guardaYDevuelve() {
        when(estudianteRepository.save(estudiante)).thenReturn(Mono.just(estudiante));

        StepVerifier.create(service.createEstudiante(estudiante))
                .expectNext(estudiante)
                .verifyComplete();
    }

    // ── CRUD Curso ───────────────────────────────────────────────────────────

    @Test
    void findAllCursos_retornaFlux() {
        when(cursoRepository.findAll()).thenReturn(Flux.just(curso));

        StepVerifier.create(service.findAllCursos())
                .expectNext(curso)
                .verifyComplete();
    }

    @Test
    void createCurso_guardaYDevuelve() {
        when(cursoRepository.save(curso)).thenReturn(Mono.just(curso));

        StepVerifier.create(service.createCurso(curso))
                .expectNext(curso)
                .verifyComplete();
    }

    // ── matricular ───────────────────────────────────────────────────────────

    @Test
    void matricular_estudianteYCursoExisten_creaMatricula() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "c1")).thenReturn(Mono.empty());
        when(matriculacionRepository.save(any(Matriculacion.class))).thenReturn(Mono.just(matriculacion));

        StepVerifier.create(service.matricular("e1", "c1"))
                .assertNext(m -> {
                    assertThat(m.getEstudianteId()).isEqualTo("e1");
                    assertThat(m.getCursoId()).isEqualTo("c1");
                })
                .verifyComplete();
    }

    @Test
    void matricular_estudianteNoExiste_lanza404() {
        when(estudianteRepository.findById("nope")).thenReturn(Mono.empty());
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));

        StepVerifier.create(service.matricular("nope", "c1"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void matricular_cursoNoExiste_lanza404() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(cursoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.matricular("e1", "nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void matricular_yaMatriculado_lanza409() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "c1")).thenReturn(Mono.just(matriculacion));

        StepVerifier.create(service.matricular("e1", "c1"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 409)
                .verify();
    }

    // ── desmatricular ────────────────────────────────────────────────────────

    @Test
    void desmatricular_matriculaExiste_eliminaYRetorna200() {
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "c1")).thenReturn(Mono.just(matriculacion));
        when(matriculacionRepository.delete(matriculacion)).thenReturn(Mono.empty());

        StepVerifier.create(service.desmatricular("e1", "c1"))
                .assertNext(response -> {
                    assertThat(response.getStatusCode().value()).isEqualTo(200);
                    assertThat(response.getBody()).isEqualTo(matriculacion);
                })
                .verifyComplete();
    }

    @Test
    void desmatricular_matriculaNoExiste_lanza404() {
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.desmatricular("e1", "nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── findCursosDeEstudiante ───────────────────────────────────────────────

    @Test
    void findCursosDeEstudiante_patronDosFlatMap_retornaCursos() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(matriculacionRepository.findByEstudianteId("e1")).thenReturn(Flux.just(matriculacion));
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));

        StepVerifier.create(service.findCursosDeEstudiante("e1"))
                .expectNext(curso)
                .verifyComplete();
    }

    @Test
    void findCursosDeEstudiante_estudianteNoExiste_lanza404() {
        when(estudianteRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findCursosDeEstudiante("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void findCursosDeEstudiante_sinMatriculas_retornaFluxVacio() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(matriculacionRepository.findByEstudianteId("e1")).thenReturn(Flux.empty());

        StepVerifier.create(service.findCursosDeEstudiante("e1"))
                .verifyComplete();
    }

    // ── findEstudiantesDeCurso ───────────────────────────────────────────────

    @Test
    void findEstudiantesDeCurso_patronSimetrico_retornaEstudiantes() {
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));
        when(matriculacionRepository.findByCursoId("c1")).thenReturn(Flux.just(matriculacion));
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));

        StepVerifier.create(service.findEstudiantesDeCurso("c1"))
                .expectNext(estudiante)
                .verifyComplete();
    }

    @Test
    void findEstudiantesDeCurso_cursoNoExiste_lanza404() {
        when(cursoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findEstudiantesDeCurso("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── findEstudianteConMatriculas ──────────────────────────────────────────

    @Test
    void findEstudianteConMatriculas_patronFlatMapZipWithCollectList() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(matriculacionRepository.findByEstudianteId("e1")).thenReturn(Flux.just(matriculacion));
        when(cursoRepository.findById("c1")).thenReturn(Mono.just(curso));

        StepVerifier.create(service.findEstudianteConMatriculas("e1"))
                .assertNext(dto -> {
                    assertThat(dto.estudiante()).isEqualTo(estudiante);
                    assertThat(dto.matriculas()).hasSize(1);
                    assertThat(dto.matriculas().get(0).curso()).isEqualTo(curso);
                    assertThat(dto.matriculas().get(0).matriculacion()).isEqualTo(matriculacion);
                })
                .verifyComplete();
    }

    @Test
    void findEstudianteConMatriculas_sinMatriculas_retornaDTOConListaVacia() {
        when(estudianteRepository.findById("e1")).thenReturn(Mono.just(estudiante));
        when(matriculacionRepository.findByEstudianteId("e1")).thenReturn(Flux.empty());

        StepVerifier.create(service.findEstudianteConMatriculas("e1"))
                .assertNext(dto -> {
                    assertThat(dto.estudiante()).isEqualTo(estudiante);
                    assertThat(dto.matriculas()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void findEstudianteConMatriculas_estudianteNoExiste_lanza404() {
        when(estudianteRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findEstudianteConMatriculas("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── actualizarNota ───────────────────────────────────────────────────────

    @Test
    void actualizarNota_matriculaExiste_actualizaYDevuelve() {
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "c1")).thenReturn(Mono.just(matriculacion));
        Matriculacion conNota = new Matriculacion("m1", "e1", "c1", matriculacion.getFechaAlta(), 9.0);
        when(matriculacionRepository.save(any(Matriculacion.class))).thenReturn(Mono.just(conNota));

        StepVerifier.create(service.actualizarNota("e1", "c1", 9.0))
                .assertNext(m -> assertThat(m.getNota()).isEqualTo(9.0))
                .verifyComplete();
    }

    @Test
    void actualizarNota_matriculaNoExiste_lanza404() {
        when(matriculacionRepository.findByEstudianteIdAndCursoId("e1", "nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.actualizarNota("e1", "nope", 5.0))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }
}
