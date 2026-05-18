package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Schema(description = "Proyecto. Cada proyecto puede tener múltiples tareas (relación 1:N).")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "proyectos")
public class Proyecto {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Nombre del proyecto", example = "Portal de clientes")
    @NotBlank
    private String nombre;
    @Schema(description = "Descripción opcional del proyecto", example = "Migración del portal legacy a React", nullable = true)
    private String descripcion;
}
