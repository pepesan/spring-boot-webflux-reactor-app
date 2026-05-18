package com.cursosdedesarrollo.webfluxapp.ejemplo.services.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.EstudianteConMatriculasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.MatriculacionConCursoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.CursoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.EstudianteRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem.MatriculacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class MatriculacionService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculacionRepository matriculacionRepository;

    // ── CRUD Estudiante ──────────────────────────────────────────────────────

    public Flux<Estudiante> findAllEstudiantes() {
        return estudianteRepository.findAll();
    }

    public Mono<Estudiante> createEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    // ── CRUD Curso ───────────────────────────────────────────────────────────

    public Flux<Curso> findAllCursos() {
        return cursoRepository.findAll();
    }

    public Mono<Curso> createCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    // ── Operaciones N:M ──────────────────────────────────────────────────────

    /**
     * Verifica con zipWith que estudiante y curso existen (en paralelo),
     * comprueba que no existe ya una matrícula (para evitar duplicados)
     * y guarda la nueva Matriculacion con la fecha de hoy.
     */
    public Mono<Matriculacion> matricular(String estudianteId, String cursoId) {
        Mono<Estudiante> estudianteMono = estudianteRepository.findById(estudianteId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado")));
        Mono<Curso> cursoMono = cursoRepository.findById(cursoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado")));

        return estudianteMono.zipWith(cursoMono)
                .flatMap(tuple ->
                        matriculacionRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId)
                                .flatMap(existing -> Mono.<Matriculacion>error(
                                        new ResponseStatusException(HttpStatus.CONFLICT, "El estudiante ya está matriculado en este curso")))
                                .switchIfEmpty(Mono.defer(() ->
                                        matriculacionRepository.save(
                                                new Matriculacion(null, estudianteId, cursoId, LocalDate.now(), null)))
                                )
                );
    }

    /**
     * Busca la matrícula para verificar que existe (lanza 404 si no),
     * luego la elimina usando la query derivada deleteByEstudianteIdAndCursoId.
     */
    public Mono<ResponseEntity<Matriculacion>> desmatricular(String estudianteId, String cursoId) {
        return matriculacionRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula no encontrada")))
                .flatMap(m -> matriculacionRepository.delete(m).then(Mono.just(ResponseEntity.ok(m))));
    }

    /**
     * flatMapMany + flatMap (dos niveles):
     * Mono<Estudiante> → Flux<Matriculacion> → Flux<Curso>
     */
    public Flux<Curso> findCursosDeEstudiante(String estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado")))
                .flatMapMany(e -> matriculacionRepository.findByEstudianteId(e.getId()))
                .flatMap(m -> cursoRepository.findById(m.getCursoId()));
    }

    /**
     * flatMapMany + flatMap (dos niveles) en sentido inverso:
     * Mono<Curso> → Flux<Matriculacion> → Flux<Estudiante>
     */
    public Flux<Estudiante> findEstudiantesDeCurso(String cursoId) {
        return cursoRepository.findById(cursoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado")))
                .flatMapMany(c -> matriculacionRepository.findByCursoId(c.getId()))
                .flatMap(m -> estudianteRepository.findById(m.getEstudianteId()));
    }

    /**
     * DTO enriquecido: para cada matrícula del estudiante se combina con su curso
     * usando zipWith dentro de un flatMap, y el resultado se agrega con collectList.
     * Patrón: flatMap → zipWith → collectList.
     */
    public Mono<EstudianteConMatriculasDTO> findEstudianteConMatriculas(String estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado")))
                .flatMap(estudiante ->
                        matriculacionRepository.findByEstudianteId(estudianteId)
                                .flatMap(m ->
                                        cursoRepository.findById(m.getCursoId())
                                                .map(curso -> new MatriculacionConCursoDTO(m, curso))
                                )
                                .collectList()
                                .map(matriculas -> new EstudianteConMatriculasDTO(estudiante, matriculas))
                );
    }

    /**
     * Actualiza la nota de una matrícula existente.
     */
    public Mono<Matriculacion> actualizarNota(String estudianteId, String cursoId, Double nota) {
        return matriculacionRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula no encontrada")))
                .flatMap(m -> {
                    m.setNota(nota);
                    return matriculacionRepository.save(m);
                });
    }
}
