package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.agregacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion.Venta;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.ResumenCategoriaDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.TopProductoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.agregacion.VentaService;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Aggregation Pipeline — Ventas",
        description = "Ejemplo de MongoDB Aggregation Pipeline con ReactiveMongoTemplate. " +
                "Demuestra $group, $project, $sort y $limit para calcular " +
                "resúmenes estadísticos y rankings directamente en el servidor MongoDB.")
@RestController
@RequestMapping("/api/agregacion")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(summary = "Listar todas las ventas")
    @ApiResponse(responseCode = "200", description = "Lista de ventas registradas")
    @GetMapping("/ventas")
    public Flux<Venta> getAllVentas() {
        return ventaService.findAll();
    }

    @Operation(summary = "Registrar una venta")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Venta registrada",
                content = @Content(schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Venta> createVenta(@Valid @RequestBody Venta venta) {
        return ventaService.create(venta);
    }

    @Operation(summary = "Resumen de ventas por categoría ($group + $project + $sort)",
            description = "Aggregation pipeline: agrupa todas las ventas por categoría calculando " +
                    "la suma total ($sum), la media ($avg) y el recuento ($count). " +
                    "La etapa $project renombra _id a categoria. " +
                    "Resultado ordenado de mayor a menor importe total.")
    @ApiResponse(responseCode = "200", description = "Resumen estadístico por categoría",
            content = @Content(schema = @Schema(implementation = ResumenCategoriaDTO.class)))
    @GetMapping("/ventas/resumen")
    public Flux<ResumenCategoriaDTO> resumenPorCategoria() {
        return ventaService.resumenPorCategoria();
    }

    @Operation(summary = "Ranking de productos por importe total ($group + $sort + $limit)",
            description = "Aggregation pipeline: agrupa por producto acumulando el importe total, " +
                    "ordena de mayor a menor y limita al top N. " +
                    "El parámetro limite define cuántos productos devolver.")
    @ApiResponse(responseCode = "200", description = "Top N productos por importe acumulado",
            content = @Content(schema = @Schema(implementation = TopProductoDTO.class)))
    @GetMapping("/ventas/top")
    public Flux<TopProductoDTO> topProductos(
            @Parameter(description = "Número de productos a devolver (por defecto 5)")
            @RequestParam(defaultValue = "5") int limite) {
        return ventaService.topProductos(limite);
    }
}
