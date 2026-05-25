package com.cursosdedesarrollo.webfluxapp.ejemplo.services.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.LineaPedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.embebido.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService service;

    private Pedido pedido;
    private LineaPedido linea;

    @BeforeEach
    void setUp() {
        linea = new LineaPedido("Teclado mecánico", 2, 89.99);
        pedido = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "PENDIENTE",
                new ArrayList<>(List.of(linea)));
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_retornaFluxDePedidos() {
        when(pedidoRepository.findAll()).thenReturn(Flux.just(pedido));

        StepVerifier.create(service.findAll())
                .assertNext(p -> assertThat(p.getId()).isEqualTo("p1"))
                .verifyComplete();
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_encontrado_retorna200() {
        when(pedidoRepository.findById("p1")).thenReturn(Mono.just(pedido));

        StepVerifier.create(service.findById("p1"))
                .assertNext(r -> {
                    assertThat(r.getStatusCode().value()).isEqualTo(200);
                    assertThat(r.getBody()).isNotNull();
                    assertThat(r.getBody().getCliente()).isEqualTo("David");
                })
                .verifyComplete();
    }

    @Test
    void findById_noEncontrado_retorna404() {
        when(pedidoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findById("nope"))
                .assertNext(r -> assertThat(r.getStatusCode().value()).isEqualTo(404))
                .verifyComplete();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_guardaYRetornaPedido() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(Mono.just(pedido));

        StepVerifier.create(service.create(pedido))
                .assertNext(p -> assertThat(p.getId()).isEqualTo("p1"))
                .verifyComplete();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_encontrado_retorna200() {
        when(pedidoRepository.findById("p1")).thenReturn(Mono.just(pedido));
        when(pedidoRepository.delete(pedido)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("p1"))
                .assertNext(r -> {
                    assertThat(r.getStatusCode().value()).isEqualTo(200);
                    assertThat(r.getBody()).isEqualTo(pedido);
                })
                .verifyComplete();
    }

    @Test
    void delete_noEncontrado_retorna404() {
        when(pedidoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("nope"))
                .assertNext(r -> assertThat(r.getStatusCode().value()).isEqualTo(404))
                .verifyComplete();
    }

    // ── findConTotal ──────────────────────────────────────────────────────────

    @Test
    void findConTotal_calculaTotalCorrectamente() {
        when(pedidoRepository.findById("p1")).thenReturn(Mono.just(pedido));

        // 2 × 89.99 = 179.98
        StepVerifier.create(service.findConTotal("p1"))
                .assertNext(dto -> {
                    assertThat(dto.pedido()).isEqualTo(pedido);
                    assertThat(dto.total()).isEqualTo(179.98);
                })
                .verifyComplete();
    }

    @Test
    void findConTotal_pedidoSinLineas_totalCero() {
        Pedido vacio = new Pedido("p2", "Ana", LocalDate.now(), "ENVIADO", new ArrayList<>());
        when(pedidoRepository.findById("p2")).thenReturn(Mono.just(vacio));

        StepVerifier.create(service.findConTotal("p2"))
                .assertNext(dto -> assertThat(dto.total()).isEqualTo(0.0))
                .verifyComplete();
    }

    @Test
    void findConTotal_noEncontrado_lanza404() {
        when(pedidoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findConTotal("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── agregarLinea ──────────────────────────────────────────────────────────

    @Test
    void agregarLinea_encontrado_anadeLineaYGuarda() {
        Pedido pedidoVacio = new Pedido("p1", "David", LocalDate.now(), "PENDIENTE", new ArrayList<>());
        LineaPedido nuevaLinea = new LineaPedido("Ratón", 1, 29.99);
        Pedido pedidoActualizado = new Pedido("p1", "David", LocalDate.now(), "PENDIENTE",
                new ArrayList<>(List.of(nuevaLinea)));

        when(pedidoRepository.findById("p1")).thenReturn(Mono.just(pedidoVacio));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(Mono.just(pedidoActualizado));

        StepVerifier.create(service.agregarLinea("p1", nuevaLinea))
                .assertNext(p -> assertThat(p.getLineas()).hasSize(1))
                .verifyComplete();
    }

    @Test
    void agregarLinea_noEncontrado_lanza404() {
        when(pedidoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.agregarLinea("nope", linea))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── findByEstado ──────────────────────────────────────────────────────────

    @Test
    void findByEstado_retornaFluxFiltrado() {
        when(pedidoRepository.findByEstado("PENDIENTE")).thenReturn(Flux.just(pedido));

        StepVerifier.create(service.findByEstado("PENDIENTE"))
                .assertNext(p -> assertThat(p.getEstado()).isEqualTo("PENDIENTE"))
                .verifyComplete();
    }

    // ── findByProducto ────────────────────────────────────────────────────────

    @Test
    void findByProducto_retornaFluxConPedidosQueContienenElProducto() {
        when(pedidoRepository.findByLineasProducto("Teclado mecánico")).thenReturn(Flux.just(pedido));

        StepVerifier.create(service.findByProducto("Teclado mecánico"))
                .assertNext(p -> assertThat(p.getLineas().get(0).getProducto()).isEqualTo("Teclado mecánico"))
                .verifyComplete();
    }

    // ── actualizarEstado ──────────────────────────────────────────────────────

    @Test
    void actualizarEstado_encontrado_actualizaYGuarda() {
        Pedido actualizado = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "ENVIADO",
                new ArrayList<>(List.of(linea)));
        when(pedidoRepository.findById("p1")).thenReturn(Mono.just(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(Mono.just(actualizado));

        StepVerifier.create(service.actualizarEstado("p1", "ENVIADO"))
                .assertNext(p -> assertThat(p.getEstado()).isEqualTo("ENVIADO"))
                .verifyComplete();
    }

    @Test
    void actualizarEstado_noEncontrado_lanza404() {
        when(pedidoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.actualizarEstado("nope", "ENVIADO"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }
}
