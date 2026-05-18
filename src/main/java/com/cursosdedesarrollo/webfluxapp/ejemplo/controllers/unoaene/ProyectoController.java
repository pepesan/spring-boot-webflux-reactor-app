package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoaene.ProyectoConTareasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoaene.ProyectoService;
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

@Tag(name = "1:N — Proyectos / Tareas",
        description = "Ejemplo de relación 1:N referenciada. Cada tarea almacena proyectoId como clave foránea. " +
                "Demuestra flatMapMany, collectList() y borrado en cascada.")
@RestController
@RequestMapping("/api/relaciones/1an")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    // ── Proyectos ────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los proyectos")
    @ApiResponse(responseCode = "200", description = "Lista de proyectos")
    @GetMapping("/proyectos")
    public Flux<Proyecto> getAllProyectos() {
        return proyectoService.findAll();
    }

    @Operation(summary = "Crear un proyecto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Proyecto creado",
                content = @Content(schema = @Schema(implementation = Proyecto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/proyectos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Proyecto> createProyecto(@Valid @RequestBody Proyecto proyecto) {
        return proyectoService.create(proyecto);
    }

    @Operation(summary = "Obtener tareas de un proyecto",
            description = "Busca el proyecto con findById y luego emite sus tareas con flatMapMany + findByProyectoId. " +
                    "Si se proporciona el parámetro 'estado', delega en la query derivada findByProyectoIdAndEstado " +
                    "que filtra directamente en MongoDB.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de tareas (puede ser vacía)",
                content = @Content(schema = @Schema(implementation = Tarea.class))),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado", content = @Content)
    })
    @GetMapping("/proyectos/{id}/tareas")
    public Flux<Tarea> getTareasByProyecto(
            @Parameter(description = "ID del proyecto") @PathVariable String id,
            @Parameter(description = "Filtrar por estado: PENDIENTE, EN_CURSO, HECHO")
            @RequestParam(required = false) String estado) {
        if (estado != null) {
            return proyectoService.findTareasByProyectoAndEstado(id, estado);
        }
        return proyectoService.findTareasByProyecto(id);
    }

    @Operation(summary = "Obtener proyecto con todas sus tareas",
            description = "Encadena flatMap + collectList(): busca el proyecto, emite todas las tareas con " +
                    "findByProyectoId y las agrega en una List<Tarea> dentro del DTO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proyecto con lista de tareas",
                content = @Content(schema = @Schema(implementation = ProyectoConTareasDTO.class))),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado", content = @Content)
    })
    @GetMapping("/proyectos/{id}/completo")
    public Mono<ProyectoConTareasDTO> getProyectoConTareas(
            @Parameter(description = "ID del proyecto") @PathVariable String id) {
        return proyectoService.findProyectoConTareas(id);
    }

    @Operation(summary = "Eliminar proyecto y todas sus tareas (cascada)",
            description = "Obtiene todas las tareas del proyecto con flatMap sobre findByProyectoId, " +
                    "las elimina una a una y luego elimina el proyecto con then().")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proyecto eliminado (y sus tareas)",
                content = @Content(schema = @Schema(implementation = Proyecto.class))),
        @ApiResponse(responseCode = "404", description = "Proyecto no encontrado", content = @Content)
    })
    @DeleteMapping("/proyectos/{id}")
    public Mono<ResponseEntity<Proyecto>> deleteProyecto(
            @Parameter(description = "ID del proyecto") @PathVariable String id) {
        return proyectoService.deleteConTareas(id);
    }

    // ── Tareas ───────────────────────────────────────────────────────────────

    @Operation(summary = "Crear una tarea",
            description = "Verifica que el proyecto referenciado en proyectoId exista antes de guardar la tarea.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tarea creada",
                content = @Content(schema = @Schema(implementation = Tarea.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Proyecto referenciado no existe", content = @Content)
    })
    @PostMapping("/tareas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Tarea> createTarea(@Valid @RequestBody Tarea tarea) {
        return proyectoService.createTarea(tarea);
    }

    @Operation(summary = "Cambiar el estado de una tarea",
            description = "Actualiza únicamente el campo estado. Valores válidos: PENDIENTE, EN_CURSO, HECHO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarea actualizada",
                content = @Content(schema = @Schema(implementation = Tarea.class))),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada", content = @Content)
    })
    @PutMapping("/tareas/{id}/estado")
    public Mono<ResponseEntity<Tarea>> updateEstado(
            @Parameter(description = "ID de la tarea") @PathVariable String id,
            @Parameter(description = "Nuevo estado: PENDIENTE, EN_CURSO, HECHO")
            @RequestParam String estado) {
        return proyectoService.updateEstado(id, estado);
    }

    @Operation(summary = "Eliminar una tarea")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarea eliminada",
                content = @Content(schema = @Schema(implementation = Tarea.class))),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada", content = @Content)
    })
    @DeleteMapping("/tareas/{id}")
    public Mono<ResponseEntity<Tarea>> deleteTarea(
            @Parameter(description = "ID de la tarea") @PathVariable String id) {
        return proyectoService.deleteTarea(id);
    }
}
