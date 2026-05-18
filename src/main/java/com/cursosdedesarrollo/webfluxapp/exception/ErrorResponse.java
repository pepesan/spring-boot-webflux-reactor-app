package com.cursosdedesarrollo.webfluxapp.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta de error estándar")
public record ErrorResponse(
        @Schema(description = "Código HTTP", example = "400") int status,
        @Schema(description = "Tipo de error", example = "Bad Request") String error,
        @Schema(description = "Mensaje descriptivo") String message,
        @Schema(description = "Detalle de campos con error (validación)") List<String> details
) {}
