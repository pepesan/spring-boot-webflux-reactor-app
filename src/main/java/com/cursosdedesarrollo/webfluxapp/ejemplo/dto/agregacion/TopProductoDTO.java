package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Resultado del pipeline $group + $sort + $limit sobre ventas. " +
        "Representa un producto del ranking por importe total acumulado.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductoDTO {

    @Schema(description = "Nombre del producto", example = "Portátil gaming")
    private String producto;

    @Schema(description = "Importe total acumulado de todas las ventas del producto", example = "12598.75")
    private Double totalImporte;
}
