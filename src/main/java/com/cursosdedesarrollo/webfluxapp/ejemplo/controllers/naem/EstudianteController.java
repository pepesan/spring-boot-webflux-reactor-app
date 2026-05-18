package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.EstudianteConMatriculasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem.MatricularRequest;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.naem.MatriculacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "N:M — Estudiantes / Cursos / Matrículas",
        description = "Ejemplo de relación N:M con colección intermedia. " +
                "Matriculacion almacena estudianteId + cursoId + datos propios (fechaAlta, nota). " +
                "Demuestra flatMapMany encadenado, zipWith de validación y collectList.")
@RestController
@RequestMapping("/api/relaciones/nm")
public class EstudianteController {

    @Autowired
    private MatriculacionService matriculacionService;

    // ── Estudiantes ──────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los estudiantes")
    @ApiResponse(responseCode = "200", description = "Lista de estudiantes")
    @GetMapping("/estudiantes")
    public Flux<Estudiante> getAllEstudiantes() {
        return matriculacionService.findAllEstudiantes();
    }

    @Operation(summary = "Crear un estudiante")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Estudiante creado",
                content = @Content(schema = @Schema(implementation = Estudiante.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/estudiantes")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Estudiante> createEstudiante(@Valid @RequestBody Estudiante estudiante) {
        return matriculacionService.createEstudiante(estudiante);
    }

    @Operation(summary = "Obtener cursos de un estudiante",
            description = "Patrón flatMapMany + flatMap (dos niveles): " +
                    "Mono<Estudiante> → Flux<Matriculacion> (findByEstudianteId) → Flux<Curso> (findById por cada cursoId).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cursos en los que está matriculado el estudiante",
                content = @Content(schema = @Schema(implementation = Curso.class))),
        @ApiResponse(responseCode = "404", description = "Estudiante no encontrado", content = @Content)
    })
    @GetMapping("/estudiantes/{id}/cursos")
    public Flux<Curso> getCursosDeEstudiante(
            @Parameter(description = "ID del estudiante") @PathVariable String id) {
        return matriculacionService.findCursosDeEstudiante(id);
    }

    @Operation(summary = "Obtener estudiante con todas sus matrículas resueltas",
            description = "Para cada matrícula se combina con su curso usando zipWith dentro de un flatMap. " +
                    "El resultado se agrega en una List con collectList(). " +
                    "Patrón: flatMap → zipWith → collectList.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estudiante con lista de MatriculacionConCursoDTO",
                content = @Content(schema = @Schema(implementation = EstudianteConMatriculasDTO.class))),
        @ApiResponse(responseCode = "404", description = "Estudiante no encontrado", content = @Content)
    })
    @GetMapping("/estudiantes/{id}/completo")
    public Mono<EstudianteConMatriculasDTO> getEstudianteConMatriculas(
            @Parameter(description = "ID del estudiante") @PathVariable String id) {
        return matriculacionService.findEstudianteConMatriculas(id);
    }

    // ── Cursos ───────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los cursos")
    @ApiResponse(responseCode = "200", description = "Lista de cursos")
    @GetMapping("/cursos")
    public Flux<Curso> getAllCursos() {
        return matriculacionService.findAllCursos();
    }

    @Operation(summary = "Crear un curso")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Curso creado",
                content = @Content(schema = @Schema(implementation = Curso.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/cursos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Curso> createCurso(@Valid @RequestBody Curso curso) {
        return matriculacionService.createCurso(curso);
    }

    @Operation(summary = "Obtener estudiantes de un curso",
            description = "Patrón simétrico al de cursos de un estudiante: " +
                    "Mono<Curso> → Flux<Matriculacion> (findByCursoId) → Flux<Estudiante>.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estudiantes matriculados en el curso",
                content = @Content(schema = @Schema(implementation = Estudiante.class))),
        @ApiResponse(responseCode = "404", description = "Curso no encontrado", content = @Content)
    })
    @GetMapping("/cursos/{id}/estudiantes")
    public Flux<Estudiante> getEstudiantesDeCurso(
            @Parameter(description = "ID del curso") @PathVariable String id) {
        return matriculacionService.findEstudiantesDeCurso(id);
    }

    // ── Matrículas ───────────────────────────────────────────────────────────

    @Operation(summary = "Matricular un estudiante en un curso",
            description = "Verifica con zipWith que estudiante y curso existen (en paralelo). " +
                    "Luego comprueba que no existe ya una matrícula para evitar duplicados (409 si ya existe). " +
                    "Asigna fechaAlta = hoy y nota = null.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Matrícula creada",
                content = @Content(schema = @Schema(implementation = Matriculacion.class))),
        @ApiResponse(responseCode = "404", description = "Estudiante o curso no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "El estudiante ya está matriculado en ese curso", content = @Content)
    })
    @PostMapping("/matriculaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Matriculacion> matricular(@Valid @RequestBody MatricularRequest request) {
        return matriculacionService.matricular(request.estudianteId(), request.cursoId());
    }

    @Operation(summary = "Desmatricular un estudiante de un curso",
            description = "Elimina la Matriculacion de la colección intermedia. " +
                    "Devuelve 404 si la matrícula no existe.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matrícula eliminada",
                content = @Content(schema = @Schema(implementation = Matriculacion.class))),
        @ApiResponse(responseCode = "404", description = "Matrícula no encontrada", content = @Content)
    })
    @DeleteMapping("/matriculaciones/{estudianteId}/{cursoId}")
    public Mono<ResponseEntity<Matriculacion>> desmatricular(
            @Parameter(description = "ID del estudiante") @PathVariable String estudianteId,
            @Parameter(description = "ID del curso") @PathVariable String cursoId) {
        return matriculacionService.desmatricular(estudianteId, cursoId);
    }

    @Operation(summary = "Actualizar la nota de una matrícula",
            description = "Modifica únicamente el campo nota de la Matriculacion existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nota actualizada",
                content = @Content(schema = @Schema(implementation = Matriculacion.class))),
        @ApiResponse(responseCode = "404", description = "Matrícula no encontrada", content = @Content)
    })
    @PutMapping("/matriculaciones/{estudianteId}/{cursoId}/nota")
    public Mono<Matriculacion> actualizarNota(
            @Parameter(description = "ID del estudiante") @PathVariable String estudianteId,
            @Parameter(description = "ID del curso") @PathVariable String cursoId,
            @Parameter(description = "Nueva nota (0.0–10.0)") @RequestParam Double nota) {
        return matriculacionService.actualizarNota(estudianteId, cursoId, nota);
    }
}
