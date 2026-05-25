package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Línea de pedido embebida dentro del documento Pedido. No tiene colección propia en MongoDB.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineaPedido {

    @Schema(description = "Nombre del producto", example = "Teclado mecánico")
    @NotBlank
    private String producto;

    @Schema(description = "Cantidad de unidades", example = "2")
    @NotNull
    @Min(1)
    private Integer cantidad;

    @Schema(description = "Precio unitario en euros", example = "89.99")
    @NotNull
    private Double precioUnitario;
}
