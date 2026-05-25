package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.embebido;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.LineaPedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido.Pedido;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.embebido.PedidoConTotalDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.embebido.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController controller;

    private WebTestClient client;
    private Pedido pedido;
    private LineaPedido linea;
    private PedidoConTotalDTO dto;

    @BeforeEach
    void setUp() {
        linea = new LineaPedido("Teclado mecánico", 2, 89.99);
        pedido = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "PENDIENTE",
                new ArrayList<>(List.of(linea)));
        dto = new PedidoConTotalDTO(pedido, 179.98);

        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @Test
    void getAllPedidos_retornaLista() {
        when(pedidoService.findAll()).thenReturn(Flux.just(pedido));

        client.get().uri("/api/relaciones/embebido/pedidos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Pedido.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getCliente()).isEqualTo("David"));
    }

    @Test
    void getPedidoById_encontrado_retorna200() {
        when(pedidoService.findById("p1")).thenReturn(Mono.just(ResponseEntity.ok(pedido)));

        client.get().uri("/api/relaciones/embebido/pedidos/p1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pedido.class)
                .value(p -> assertThat(p.getCliente()).isEqualTo("David"));
    }

    @Test
    void getPedidoById_noEncontrado_retorna404() {
        when(pedidoService.findById("nope")).thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.get().uri("/api/relaciones/embebido/pedidos/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPedido_retorna201() {
        when(pedidoService.create(any(Pedido.class))).thenReturn(Mono.just(pedido));

        client.post().uri("/api/relaciones/embebido/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"cliente\":\"David\",\"fecha\":\"2024-01-15\",\"estado\":\"PENDIENTE\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Pedido.class)
                .value(p -> assertThat(p.getId()).isEqualTo("p1"));
    }

    @Test
    void deletePedido_encontrado_retorna200() {
        when(pedidoService.delete("p1")).thenReturn(Mono.just(ResponseEntity.ok(pedido)));

        client.delete().uri("/api/relaciones/embebido/pedidos/p1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pedido.class)
                .value(p -> assertThat(p.getId()).isEqualTo("p1"));
    }

    @Test
    void deletePedido_noEncontrado_retorna404() {
        when(pedidoService.delete("nope")).thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.delete().uri("/api/relaciones/embebido/pedidos/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Operaciones sobre documentos embebidos ────────────────────────────────

    @Test
    void getPedidoConTotal_encontrado_retornaDTO() {
        when(pedidoService.findConTotal("p1")).thenReturn(Mono.just(dto));

        client.get().uri("/api/relaciones/embebido/pedidos/p1/total")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PedidoConTotalDTO.class)
                .value(d -> {
                    assertThat(d.pedido().getId()).isEqualTo("p1");
                    assertThat(d.total()).isEqualTo(179.98);
                });
    }

    @Test
    void getPedidoConTotal_noEncontrado_retorna404() {
        when(pedidoService.findConTotal("nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Pedido no encontrado")));

        client.get().uri("/api/relaciones/embebido/pedidos/nope/total")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void agregarLinea_encontrado_retornaPedidoActualizado() {
        Pedido conDosLineas = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "PENDIENTE",
                new ArrayList<>(List.of(linea, new LineaPedido("Ratón", 1, 29.99))));
        when(pedidoService.agregarLinea(eq("p1"), any(LineaPedido.class))).thenReturn(Mono.just(conDosLineas));

        client.post().uri("/api/relaciones/embebido/pedidos/p1/lineas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"producto\":\"Ratón\",\"cantidad\":1,\"precioUnitario\":29.99}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pedido.class)
                .value(p -> assertThat(p.getLineas()).hasSize(2));
    }

    @Test
    void agregarLinea_noEncontrado_retorna404() {
        when(pedidoService.agregarLinea(eq("nope"), any(LineaPedido.class)))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Pedido no encontrado")));

        client.post().uri("/api/relaciones/embebido/pedidos/nope/lineas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"producto\":\"Ratón\",\"cantidad\":1,\"precioUnitario\":29.99}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPedidosByEstado_retornaLista() {
        when(pedidoService.findByEstado("PENDIENTE")).thenReturn(Flux.just(pedido));

        client.get().uri("/api/relaciones/embebido/pedidos/porEstado/PENDIENTE")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Pedido.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getEstado()).isEqualTo("PENDIENTE"));
    }

    @Test
    void getPedidosByProducto_retornaLista() {
        when(pedidoService.findByProducto("Teclado mecánico")).thenReturn(Flux.just(pedido));

        client.get().uri("/api/relaciones/embebido/pedidos/porProducto/Teclado mecánico")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Pedido.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getLineas().get(0).getProducto()).isEqualTo("Teclado mecánico"));
    }

    @Test
    void actualizarEstado_encontrado_retorna200() {
        Pedido enviado = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "ENVIADO",
                new ArrayList<>(List.of(linea)));
        when(pedidoService.actualizarEstado("p1", "ENVIADO")).thenReturn(Mono.just(enviado));

        client.put().uri("/api/relaciones/embebido/pedidos/p1/estado?estado=ENVIADO")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pedido.class)
                .value(p -> assertThat(p.getEstado()).isEqualTo("ENVIADO"));
    }

    @Test
    void actualizarEstado_noEncontrado_retorna404() {
        when(pedidoService.actualizarEstado("nope", "ENVIADO"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Pedido no encontrado")));

        client.put().uri("/api/relaciones/embebido/pedidos/nope/estado?estado=ENVIADO")
                .exchange()
                .expectStatus().isNotFound();
    }
}
