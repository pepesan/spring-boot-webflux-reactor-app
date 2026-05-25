package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Resultado del pipeline $group sobre la colección ventas. " +
        "Contiene el resumen estadístico por categoría.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenCategoriaDTO {

    @Schema(description = "Nombre de la categoría", example = "ELECTRONICA")
    private String categoria;

    @Schema(description = "Suma total de importes de la categoría", example = "15420.50")
    private Double totalVentas;

    @Schema(description = "Importe medio por venta en la categoría", example = "856.69")
    private Double mediaImporte;

    @Schema(description = "Número de ventas registradas en la categoría", example = "18")
    private Integer numVentas;
}
