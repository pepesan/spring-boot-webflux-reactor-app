package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Schema(description = "Curso al que pueden matricularse varios estudiantes (relación N:M vía colección matriculaciones).")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cursos")
public class Curso {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d2", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Título del curso", example = "Spring Boot con WebFlux")
    @NotBlank
    private String titulo;
    @Schema(description = "Descripción opcional del curso", example = "Programación reactiva con Project Reactor", nullable = true)
    private String descripcion;
    @Schema(description = "Número máximo de plazas", example = "30")
    @NotNull
    @Min(1)
    private Integer plazas;
}
