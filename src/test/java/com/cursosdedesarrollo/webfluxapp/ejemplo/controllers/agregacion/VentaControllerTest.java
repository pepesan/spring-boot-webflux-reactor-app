package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.agregacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion.Venta;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.ResumenCategoriaDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.TopProductoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.agregacion.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaController controller;

    private WebTestClient client;
    private Venta venta;

    @BeforeEach
    void setUp() {
        venta = new Venta("v1", "Portátil gaming", "ELECTRONICA", 1299.99, LocalDate.of(2024, 1, 15));
        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    @Test
    void getAllVentas_retornaLista() {
        when(ventaService.findAll()).thenReturn(Flux.just(venta));

        client.get().uri("/api/agregacion/ventas")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Venta.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getProducto()).isEqualTo("Portátil gaming"));
    }

    @Test
    void createVenta_retorna201() {
        when(ventaService.create(any(Venta.class))).thenReturn(Mono.just(venta));

        client.post().uri("/api/agregacion/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"producto\":\"Portátil gaming\",\"categoria\":\"ELECTRONICA\",\"importe\":1299.99,\"fecha\":\"2024-01-15\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Venta.class)
                .value(v -> assertThat(v.getCategoria()).isEqualTo("ELECTRONICA"));
    }

    @Test
    void createVenta_datosInvalidos_retorna400() {
        client.post().uri("/api/agregacion/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"producto\":\"\",\"categoria\":\"ELECTRONICA\",\"importe\":1299.99,\"fecha\":\"2024-01-15\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void resumenPorCategoria_retornaFluxDeResumenes() {
        ResumenCategoriaDTO dto = new ResumenCategoriaDTO("ELECTRONICA", 15420.50, 856.69, 18);
        when(ventaService.resumenPorCategoria()).thenReturn(Flux.just(dto));

        client.get().uri("/api/agregacion/ventas/resumen")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResumenCategoriaDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getCategoria()).isEqualTo("ELECTRONICA");
                    assertThat(list.get(0).getTotalVentas()).isEqualTo(15420.50);
                    assertThat(list.get(0).getNumVentas()).isEqualTo(18);
                });
    }

    @Test
    void resumenPorCategoria_sinDatos_retornaListaVacia() {
        when(ventaService.resumenPorCategoria()).thenReturn(Flux.empty());

        client.get().uri("/api/agregacion/ventas/resumen")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResumenCategoriaDTO.class)
                .hasSize(0);
    }

    @Test
    void topProductos_conLimitePorDefecto_retornaRanking() {
        TopProductoDTO top = new TopProductoDTO("Portátil gaming", 12598.75);
        when(ventaService.topProductos(5)).thenReturn(Flux.just(top));

        client.get().uri("/api/agregacion/ventas/top")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TopProductoDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getProducto()).isEqualTo("Portátil gaming");
                    assertThat(list.get(0).getTotalImporte()).isEqualTo(12598.75);
                });
    }

    @Test
    void topProductos_conLimitePersonalizado_delegaElParametro() {
        TopProductoDTO top = new TopProductoDTO("Zapatillas", 4500.00);
        when(ventaService.topProductos(3)).thenReturn(Flux.just(top));

        client.get().uri("/api/agregacion/ventas/top?limite=3")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TopProductoDTO.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getProducto()).isEqualTo("Zapatillas"));
    }

    @Test
    void topProductos_sinDatos_retornaListaVacia() {
        when(ventaService.topProductos(5)).thenReturn(Flux.empty());

        client.get().uri("/api/agregacion/ventas/top")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TopProductoDTO.class)
                .hasSize(0);
    }
}
