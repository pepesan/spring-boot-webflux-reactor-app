package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Petición para matricular un estudiante en un curso.")
public record MatricularRequest(
        @Schema(description = "ID del estudiante", example = "6651a2b3c4d5e6f7a8b9c0d1") @NotBlank String estudianteId,
        @Schema(description = "ID del curso", example = "6651a2b3c4d5e6f7a8b9c0d2") @NotBlank String cursoId
) {}
