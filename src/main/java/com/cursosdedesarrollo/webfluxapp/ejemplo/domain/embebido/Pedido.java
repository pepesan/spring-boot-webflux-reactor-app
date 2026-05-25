package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Pedido con líneas embebidas. Las líneas se almacenan como subdocumentos " +
        "directamente dentro del documento MongoDB, sin colección separada.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pedidos")
public class Pedido {

    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;

    @Schema(description = "Nombre del cliente", example = "David Vaquero")
    @NotBlank
    private String cliente;

    @Schema(description = "Fecha del pedido (ISO 8601)", example = "2024-01-15")
    @NotNull
    private LocalDate fecha;

    @Schema(description = "Estado del pedido", example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "ENVIADO", "ENTREGADO"})
    @NotBlank
    private String estado;

    @Schema(description = "Líneas del pedido embebidas directamente en el documento")
    @Valid
    private List<LineaPedido> lineas = new ArrayList<>();
}
