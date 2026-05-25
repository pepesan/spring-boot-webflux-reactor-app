package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.LineaPedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.embebido.PedidoConTotalDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.embebido.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Embebido — Pedidos / Líneas",
        description = "Ejemplo de embedding en MongoDB. Las líneas del pedido se almacenan como " +
                "subdocumentos embebidos dentro del propio documento Pedido, sin colección separada. " +
                "Las operaciones demuestran map para cálculos síncronos sobre datos ya cargados " +
                "y flatMap de escritura para modificar la lista embebida.")
@RestController
@RequestMapping("/api/relaciones/embebido")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Listar todos los pedidos")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos con sus líneas embebidas")
    @GetMapping("/pedidos")
    public Flux<Pedido> getAllPedidos() {
        return pedidoService.findAll();
    }

    @Operation(summary = "Obtener un pedido por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @GetMapping("/pedidos/{id}")
    public Mono<ResponseEntity<Pedido>> getPedidoById(
            @Parameter(description = "ID del pedido") @PathVariable String id) {
        return pedidoService.findById(id);
    }

    @Operation(summary = "Crear un pedido (con líneas opcionales)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado",
                content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/pedidos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Pedido> createPedido(@Valid @RequestBody Pedido pedido) {
        return pedidoService.create(pedido);
    }

    @Operation(summary = "Eliminar un pedido")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido eliminado",
                content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @DeleteMapping("/pedidos/{id}")
    public Mono<ResponseEntity<Pedido>> deletePedido(
            @Parameter(description = "ID del pedido") @PathVariable String id) {
        return pedidoService.delete(id);
    }

    // ── Operaciones sobre documentos embebidos ────────────────────────────────

    @Operation(summary = "Obtener pedido con importe total calculado",
            description = "Usa map (transformación síncrona) para calcular el total suma(cantidad × precioUnitario) " +
                    "sobre las líneas ya embebidas en el documento. No requiere segunda query.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido con total calculado",
                content = @Content(schema = @Schema(implementation = PedidoConTotalDTO.class))),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @GetMapping("/pedidos/{id}/total")
    public Mono<PedidoConTotalDTO> getPedidoConTotal(
            @Parameter(description = "ID del pedido") @PathVariable String id) {
        return pedidoService.findConTotal(id);
    }

    @Operation(summary = "Añadir una línea a un pedido existente",
            description = "Usa flatMap de escritura: busca el pedido, añade la línea a la lista embebida " +
                    "y persiste el documento completo con un único save(). No crea documentos secundarios.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido actualizado con la nueva línea",
                content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/pedidos/{id}/lineas")
    public Mono<Pedido> agregarLinea(
            @Parameter(description = "ID del pedido") @PathVariable String id,
            @Valid @RequestBody LineaPedido linea) {
        return pedidoService.agregarLinea(id, linea);
    }

    @Operation(summary = "Filtrar pedidos por estado",
            description = "Query derivada sobre el campo raíz estado.")
    @ApiResponse(responseCode = "200", description = "Pedidos en ese estado")
    @GetMapping("/pedidos/porEstado/{estado}")
    public Flux<Pedido> getPedidosByEstado(
            @Parameter(description = "Estado: PENDIENTE, ENVIADO o ENTREGADO") @PathVariable String estado) {
        return pedidoService.findByEstado(estado);
    }

    @Operation(summary = "Buscar pedidos que contengan un producto en sus líneas",
            description = "Query sobre subdocumento embebido (lineas.producto). MongoDB evalúa " +
                    "el filtro directamente sobre el array embebido sin necesidad de colección separada.")
    @ApiResponse(responseCode = "200", description = "Pedidos que incluyen el producto")
    @GetMapping("/pedidos/porProducto/{producto}")
    public Flux<Pedido> getPedidosByProducto(
            @Parameter(description = "Nombre exacto del producto a buscar") @PathVariable String producto) {
        return pedidoService.findByProducto(producto);
    }

    @Operation(summary = "Actualizar el estado de un pedido",
            description = "flatMap de escritura: busca el pedido, cambia el estado y persiste.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido con estado actualizado",
                content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @PutMapping("/pedidos/{id}/estado")
    public Mono<Pedido> actualizarEstado(
            @Parameter(description = "ID del pedido") @PathVariable String id,
            @Parameter(description = "Nuevo estado: PENDIENTE, ENVIADO o ENTREGADO")
            @RequestParam String estado) {
        return pedidoService.actualizarEstado(id, estado);
    }
}
