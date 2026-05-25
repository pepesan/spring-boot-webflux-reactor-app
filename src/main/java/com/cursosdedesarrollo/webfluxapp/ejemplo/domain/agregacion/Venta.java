package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Schema(description = "Registro de venta. La colección ventas sirve de base para las " +
        "aggregation pipelines que calculan resúmenes y rankings.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ventas")
public class Venta {

    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;

    @Schema(description = "Nombre del producto vendido", example = "Portátil gaming")
    @NotBlank
    private String producto;

    @Schema(description = "Categoría del producto", example = "ELECTRONICA",
            allowableValues = {"ELECTRONICA", "ROPA", "HOGAR", "DEPORTE"})
    @NotBlank
    private String categoria;

    @Schema(description = "Importe de la venta en euros", example = "1299.99")
    @NotNull
    private Double importe;

    @Schema(description = "Fecha de la venta (ISO 8601)", example = "2024-01-15")
    @NotNull
    private LocalDate fecha;
}
