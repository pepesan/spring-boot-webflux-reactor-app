package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Petición para crear un empleado y su contrato vinculados en una sola operación.")
public record CrearEmpleadoConContratoRequest(
        @Schema(description = "Datos del empleado a crear") @NotNull @Valid Empleado empleado,
        @Schema(description = "Datos del contrato a crear") @NotNull @Valid Contrato contrato
) {}
