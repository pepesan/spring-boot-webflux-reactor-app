package com.cursosdedesarrollo.webfluxapp.ejemplo.services.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.LineaPedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.embebido.PedidoConTotalDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.embebido.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Flux<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Mono<ResponseEntity<Pedido>> findById(String id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<Pedido> create(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public Mono<ResponseEntity<Pedido>> delete(String id) {
        return pedidoRepository.findById(id)
                .flatMap(p -> pedidoRepository.delete(p).then(Mono.just(ResponseEntity.ok(p))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ── Operaciones sobre documentos embebidos ────────────────────────────────

    /**
     * Calcula el total multiplicando cantidad × precioUnitario de cada línea embebida.
     * Usa map (transformación síncrona) porque el cálculo no requiere I/O:
     * el Pedido ya contiene todas sus líneas embebidas en un único documento.
     */
    public Mono<PedidoConTotalDTO> findConTotal(String id) {
        return pedidoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")))
                .map(pedido -> {
                    double total = pedido.getLineas().stream()
                            .mapToDouble(l -> l.getCantidad() * l.getPrecioUnitario())
                            .sum();
                    return new PedidoConTotalDTO(pedido, total);
                });
    }

    /**
     * Añade una línea al pedido y persiste el documento completo.
     * Usa flatMap de escritura: busca el pedido, modifica la lista embebida
     * y vuelve a guardar el documento en una sola operación save().
     * No hace falta actualizar una colección secundaria porque las líneas son embebidas.
     */
    public Mono<Pedido> agregarLinea(String id, LineaPedido linea) {
        return pedidoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")))
                .flatMap(pedido -> {
                    pedido.getLineas().add(linea);
                    return pedidoRepository.save(pedido);
                });
    }

    /**
     * Filtra pedidos por estado usando una query derivada sobre el campo raíz.
     */
    public Flux<Pedido> findByEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    /**
     * Busca pedidos que contengan un producto en sus líneas embebidas.
     * MongoDB ejecuta la query sobre el subdocumento (lineas.producto)
     * sin necesidad de colección separada ni JOIN.
     */
    public Flux<Pedido> findByProducto(String producto) {
        return pedidoRepository.findByLineasProducto(producto);
    }

    /**
     * Actualiza el estado de un pedido con flatMap de escritura.
     */
    public Mono<Pedido> actualizarEstado(String id, String estado) {
        return pedidoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado")))
                .flatMap(pedido -> {
                    pedido.setEstado(estado);
                    return pedidoRepository.save(pedido);
                });
    }
}
