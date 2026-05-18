package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Estudiante con todas sus matrículas resueltas. " +
        "Resultado de flatMap + zipWith + collectList: para cada matrícula se resuelve el curso completo.")
public record EstudianteConMatriculasDTO(
        @Schema(description = "Datos del estudiante") Estudiante estudiante,
        @Schema(description = "Lista de matrículas con datos de curso incluidos") List<MatriculacionConCursoDTO> matriculas
) {}
