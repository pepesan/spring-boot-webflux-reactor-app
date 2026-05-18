package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Schema(description = "Contrato laboral de un empleado, almacenado en colección separada.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "contratos")
public class Contrato {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d2", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Tipo de contrato", example = "INDEFINIDO", allowableValues = {"INDEFINIDO", "TEMPORAL", "PRACTICAS"})
    @NotBlank
    private String tipo;
    @Schema(description = "Fecha de inicio del contrato (ISO 8601)", example = "2024-01-15")
    @NotNull
    private LocalDate fechaInicio;
    @Schema(description = "Fecha de fin del contrato. null para contratos indefinidos.", example = "2025-12-31", nullable = true)
    private LocalDate fechaFin;
    @Schema(description = "Salario bruto anual en euros", example = "35000.0")
    @NotNull
    private Double salario;
}
