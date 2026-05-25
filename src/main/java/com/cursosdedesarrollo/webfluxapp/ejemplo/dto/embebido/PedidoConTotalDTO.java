package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido con el importe total calculado a partir de las líneas embebidas.")
public record PedidoConTotalDTO(
        @Schema(description = "Datos del pedido con sus líneas embebidas") Pedido pedido,
        @Schema(description = "Importe total del pedido en euros (suma de cantidad × precioUnitario)", example = "179.98") Double total
) {}
