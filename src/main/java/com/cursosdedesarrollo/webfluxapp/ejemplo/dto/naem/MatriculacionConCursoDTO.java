package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Matrícula enriquecida con los datos completos del curso. " +
        "Resultado de combinar Matriculacion + Curso con zipWith dentro de un flatMap.")
public record MatriculacionConCursoDTO(
        @Schema(description = "Datos de la matrícula (incluye fechaAlta y nota)") Matriculacion matriculacion,
        @Schema(description = "Datos completos del curso") Curso curso
) {}
