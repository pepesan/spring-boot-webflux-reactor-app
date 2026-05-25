package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Schema(description = "Notificación del sistema persistida en MongoDB. " +
        "Los cambios en esta colección son emitidos en tiempo real via Change Stream + SSE.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notificaciones")
public class Notificacion {

    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;

    @Schema(description = "Título de la notificación", example = "Sistema actualizado")
    @NotBlank
    private String titulo;

    @Schema(description = "Mensaje descriptivo", example = "La versión 2.0 ya está disponible")
    @NotBlank
    private String mensaje;

    @Schema(description = "Tipo de notificación", example = "INFO",
            allowableValues = {"INFO", "ALERTA", "ERROR"})
    @NotBlank
    private String tipo;

    @Schema(description = "Fecha y hora de creación (ISO 8601)", example = "2024-01-15T10:30:00")
    @NotNull
    private LocalDateTime fecha;
}
