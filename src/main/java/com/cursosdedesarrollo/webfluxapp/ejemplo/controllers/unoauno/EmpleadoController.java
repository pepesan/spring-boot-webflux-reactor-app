package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno.CrearEmpleadoConContratoRequest;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno.EmpleadoConContratoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoauno.EmpleadoService;
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

@Tag(name = "1:1 — Empleados / Contratos",
        description = "Ejemplo de relación 1:1 referenciada entre dos colecciones MongoDB. " +
                "Un empleado almacena el ID de su contrato (contratoId). " +
                "Las operaciones complejas demuestran flatMap encadenado y zipWith.")
@RestController
@RequestMapping("/api/relaciones/1a1")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    // ── Empleados ────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los empleados")
    @ApiResponse(responseCode = "200", description = "Lista de empleados")
    @GetMapping("/empleados")
    public Flux<Empleado> getAllEmpleados() {
        return empleadoService.findAllEmpleados();
    }

    @Operation(summary = "Crear un empleado sin contrato")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empleado creado",
                content = @Content(schema = @Schema(implementation = Empleado.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/empleados")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Empleado> createEmpleado(@Valid @RequestBody Empleado empleado) {
        return empleadoService.createEmpleado(empleado);
    }

    @Operation(summary = "Obtener empleado con su contrato resuelto",
            description = "Realiza dos queries encadenadas con flatMap: primero busca el empleado, " +
                    "luego resuelve el contrato por contratoId. Si el empleado no tiene contrato asignado, " +
                    "devuelve contrato=null en lugar de un error.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empleado con contrato (contrato puede ser null)",
                content = @Content(schema = @Schema(implementation = EmpleadoConContratoDTO.class))),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado", content = @Content)
    })
    @GetMapping("/empleados/{id}/completo")
    public Mono<EmpleadoConContratoDTO> getEmpleadoConContrato(
            @Parameter(description = "ID del empleado") @PathVariable String id) {
        return empleadoService.findWithContrato(id);
    }

    @Operation(summary = "Crear empleado y contrato en una sola operación",
            description = "Guarda el contrato primero (obtiene el ID generado por MongoDB) y luego " +
                    "guarda el empleado con ese ID referenciado. Demuestra flatMap de escritura encadenada.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empleado y contrato creados y vinculados",
                content = @Content(schema = @Schema(implementation = EmpleadoConContratoDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/empleados/con-contrato")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EmpleadoConContratoDTO> createEmpleadoConContrato(
            @Valid @RequestBody CrearEmpleadoConContratoRequest request) {
        return empleadoService.crearConContrato(request.empleado(), request.contrato());
    }

    @Operation(summary = "Asignar contrato existente a empleado existente",
            description = "Recupera empleado y contrato en paralelo con zipWith, " +
                    "luego actualiza contratoId en el empleado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato asignado correctamente",
                content = @Content(schema = @Schema(implementation = EmpleadoConContratoDTO.class))),
        @ApiResponse(responseCode = "404", description = "Empleado o contrato no encontrado", content = @Content)
    })
    @PutMapping("/empleados/{empleadoId}/contrato/{contratoId}")
    public Mono<EmpleadoConContratoDTO> asignarContrato(
            @Parameter(description = "ID del empleado") @PathVariable String empleadoId,
            @Parameter(description = "ID del contrato") @PathVariable String contratoId) {
        return empleadoService.asignarContrato(empleadoId, contratoId);
    }

    @Operation(summary = "Eliminar empleado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empleado eliminado",
                content = @Content(schema = @Schema(implementation = Empleado.class))),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado", content = @Content)
    })
    @DeleteMapping("/empleados/{id}")
    public Mono<ResponseEntity<Empleado>> deleteEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable String id) {
        return empleadoService.deleteEmpleado(id);
    }

    // ── Contratos ────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los contratos")
    @ApiResponse(responseCode = "200", description = "Lista de contratos")
    @GetMapping("/contratos")
    public Flux<Contrato> getAllContratos() {
        return empleadoService.findAllContratos();
    }

    @Operation(summary = "Obtener contrato por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato encontrado",
                content = @Content(schema = @Schema(implementation = Contrato.class))),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado", content = @Content)
    })
    @GetMapping("/contratos/{id}")
    public Mono<ResponseEntity<Contrato>> getContrato(
            @Parameter(description = "ID del contrato") @PathVariable String id) {
        return empleadoService.getContrato(id);
    }

    @Operation(summary = "Crear un contrato")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contrato creado",
                content = @Content(schema = @Schema(implementation = Contrato.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/contratos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Contrato> createContrato(@Valid @RequestBody Contrato contrato) {
        return empleadoService.createContrato(contrato);
    }

    @Operation(summary = "Eliminar contrato")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrato eliminado",
                content = @Content(schema = @Schema(implementation = Contrato.class))),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado", content = @Content)
    })
    @DeleteMapping("/contratos/{id}")
    public Mono<ResponseEntity<Contrato>> deleteContrato(
            @Parameter(description = "ID del contrato") @PathVariable String id) {
        return empleadoService.deleteContrato(id);
    }
}
