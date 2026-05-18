package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Schema(description = "Tarea asociada a un proyecto. Almacena el proyectoId como referencia (FK en el hijo).")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tareas")
public class Tarea {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d3", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Título descriptivo de la tarea", example = "Diseñar pantalla de login")
    @NotBlank
    private String titulo;
    @Schema(description = "Estado actual de la tarea", example = "PENDIENTE", allowableValues = {"PENDIENTE", "EN_CURSO", "HECHO"})
    @NotBlank
    private String estado;
    @Schema(description = "ID del proyecto al que pertenece esta tarea", example = "6651a2b3c4d5e6f7a8b9c0d1")
    @NotBlank
    private String proyectoId;
}
