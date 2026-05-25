package com.cursosdedesarrollo.webfluxapp.ejemplo.services.agregacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion.Venta;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.ResumenCategoriaDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.TopProductoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.agregacion.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<Venta> findAll() {
        return ventaRepository.findAll();
    }

    public Mono<Venta> create(Venta venta) {
        return ventaRepository.save(venta);
    }

    /**
     * Aggregation pipeline: $group + $project + $sort.
     * Agrupa todas las ventas por categoría y calcula la suma total,
     * la media y el recuento usando operadores de acumulación de MongoDB.
     * La etapa $project renombra _id a categoria y excluye el campo original.
     */
    public Flux<ResumenCategoriaDTO> resumenPorCategoria() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("categoria")
                        .sum("importe").as("totalVentas")
                        .avg("importe").as("mediaImporte")
                        .count().as("numVentas"),
                Aggregation.project("totalVentas", "mediaImporte", "numVentas")
                        .and("_id").as("categoria")
                        .andExclude("_id"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalVentas"))
        );
        return reactiveMongoTemplate.aggregate(aggregation, "ventas", ResumenCategoriaDTO.class);
    }

    /**
     * Aggregation pipeline: $group + $project + $sort + $limit.
     * Agrupa por producto acumulando el importe total, ordena de mayor a menor
     * y limita a los primeros N resultados.
     */
    public Flux<TopProductoDTO> topProductos(int limite) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("producto").sum("importe").as("totalImporte"),
                Aggregation.project("totalImporte")
                        .and("_id").as("producto")
                        .andExclude("_id"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalImporte")),
                Aggregation.limit(limite)
        );
        return reactiveMongoTemplate.aggregate(aggregation, "ventas", TopProductoDTO.class);
    }
}
