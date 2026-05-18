package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Empleado con su contrato resuelto. contrato puede ser null si no tiene ninguno asignado.")
public record EmpleadoConContratoDTO(
        @Schema(description = "Datos del empleado") Empleado empleado,
        @Schema(description = "Contrato asociado, null si no tiene") Contrato contrato
) {}
