package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PedidoRepository extends ReactiveMongoRepository<Pedido, String> {

    Flux<Pedido> findByEstado(String estado);

    // Query sobre campo embebido: busca pedidos que contengan el producto en sus líneas
    Flux<Pedido> findByLineasProducto(String producto);
}
