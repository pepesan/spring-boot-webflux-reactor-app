package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Schema(description = "Empleado de la empresa. Almacena el ID del contrato como referencia (1:1).")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "empleados")
public class Empleado {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Nombre completo del empleado", example = "Ana García")
    @NotBlank
    private String nombre;
    @Schema(description = "Puesto de trabajo", example = "Desarrolladora Senior")
    @NotBlank
    private String puesto;
    @Schema(description = "ID del contrato asociado. null si el empleado aún no tiene contrato asignado.", example = "6651a2b3c4d5e6f7a8b9c0d2", nullable = true)
    private String contratoId;
}
