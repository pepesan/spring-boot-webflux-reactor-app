package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Schema(description = "Colección intermedia N:M. Representa la matrícula de un estudiante en un curso. " +
        "Puede contener datos propios de la relación: fecha de alta y nota final.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matriculaciones")
public class Matriculacion {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d5", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "ID del estudiante matriculado", example = "6651a2b3c4d5e6f7a8b9c0d1")
    @NotBlank
    private String estudianteId;
    @Schema(description = "ID del curso en el que se matricula", example = "6651a2b3c4d5e6f7a8b9c0d2")
    @NotBlank
    private String cursoId;
    @Schema(description = "Fecha en que se realizó la matrícula (ISO 8601)", example = "2024-09-01")
    @NotNull
    private LocalDate fechaAlta;
    @Schema(description = "Nota final del estudiante en el curso (0–10). null si aún no se ha calificado.", example = "8.5", nullable = true)
    private Double nota;
}
