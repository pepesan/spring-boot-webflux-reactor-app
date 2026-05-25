package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.changestream;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream.Notificacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.changestream.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Change Stream — Notificaciones",
        description = "Ejemplo de MongoDB Change Stream combinado con Server-Sent Events. " +
                "Al crear una notificación via POST, cualquier cliente conectado al endpoint /stream " +
                "la recibe automáticamente sin hacer polling. " +
                "Requiere MongoDB con Replica Set o Sharded Cluster.")
@RestController
@RequestMapping("/api/changestream")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Operation(summary = "Listar todas las notificaciones")
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones almacenadas")
    @GetMapping("/notificaciones")
    public Flux<Notificacion> getAllNotificaciones() {
        return notificacionService.findAll();
    }

    @Operation(summary = "Crear una notificación",
            description = "Persiste la notificación en MongoDB. Si hay clientes suscritos al endpoint " +
                    "/stream, recibirán el documento automáticamente via Change Stream.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación creada",
                content = @Content(schema = @Schema(implementation = Notificacion.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Notificacion> createNotificacion(@Valid @RequestBody Notificacion notificacion) {
        return notificacionService.create(notificacion);
    }

    @Operation(summary = "Stream SSE de notificaciones en tiempo real (Change Stream)",
            description = "Abre un Flux infinito respaldado por un MongoDB Change Stream. " +
                    "Cada vez que se inserta o modifica un documento en la colección notificaciones, " +
                    "el evento llega aquí y se emite como SSE al cliente sin necesidad de polling. " +
                    "Mantén la conexión abierta con curl -N o EventSource en el navegador. " +
                    "Ctrl+C para cerrar.")
    @ApiResponse(responseCode = "200", description = "Stream SSE infinito de notificaciones",
            content = @Content(schema = @Schema(implementation = Notificacion.class)))
    @GetMapping(value = "/notificaciones/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notificacion> streamNotificaciones() {
        return notificacionService.stream();
    }
}
