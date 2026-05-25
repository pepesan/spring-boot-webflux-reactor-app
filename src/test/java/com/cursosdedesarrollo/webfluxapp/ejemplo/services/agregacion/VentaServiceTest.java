package com.cursosdedesarrollo.webfluxapp.ejemplo.services.agregacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion.Venta;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.ResumenCategoriaDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.agregacion.TopProductoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.agregacion.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @InjectMocks
    private VentaService service;

    private Venta venta;

    @BeforeEach
    void setUp() {
        venta = new Venta("v1", "Portátil gaming", "ELECTRONICA", 1299.99, LocalDate.of(2024, 1, 15));
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_retornaFluxDeVentas() {
        when(ventaRepository.findAll()).thenReturn(Flux.just(venta));

        StepVerifier.create(service.findAll())
                .assertNext(v -> assertThat(v.getProducto()).isEqualTo("Portátil gaming"))
                .verifyComplete();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_guardaYRetornaVenta() {
        when(ventaRepository.save(venta)).thenReturn(Mono.just(venta));

        StepVerifier.create(service.create(venta))
                .assertNext(v -> assertThat(v.getId()).isEqualTo("v1"))
                .verifyComplete();
    }

    // ── resumenPorCategoria ───────────────────────────────────────────────────

    @Test
    void resumenPorCategoria_delegaAMongoTemplateYRetornaResultados() {
        ResumenCategoriaDTO electronica = new ResumenCategoriaDTO("ELECTRONICA", 15420.50, 856.69, 18);
        ResumenCategoriaDTO ropa = new ResumenCategoriaDTO("ROPA", 3200.00, 160.00, 20);

        when(reactiveMongoTemplate.aggregate(any(Aggregation.class), eq("ventas"), eq(ResumenCategoriaDTO.class)))
                .thenReturn(Flux.just(electronica, ropa));

        StepVerifier.create(service.resumenPorCategoria())
                .assertNext(r -> {
                    assertThat(r.getCategoria()).isEqualTo("ELECTRONICA");
                    assertThat(r.getTotalVentas()).isEqualTo(15420.50);
                    assertThat(r.getNumVentas()).isEqualTo(18);
                })
                .assertNext(r -> assertThat(r.getCategoria()).isEqualTo("ROPA"))
                .verifyComplete();
    }

    @Test
    void resumenPorCategoria_sinVentas_retornaFluxVacio() {
        when(reactiveMongoTemplate.aggregate(any(Aggregation.class), eq("ventas"), eq(ResumenCategoriaDTO.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.resumenPorCategoria())
                .verifyComplete();
    }

    // ── topProductos ──────────────────────────────────────────────────────────

    @Test
    void topProductos_delegaAMongoTemplateYRetornaRanking() {
        TopProductoDTO top1 = new TopProductoDTO("Portátil gaming", 12598.75);
        TopProductoDTO top2 = new TopProductoDTO("Monitor 4K", 8999.00);

        when(reactiveMongoTemplate.aggregate(any(Aggregation.class), eq("ventas"), eq(TopProductoDTO.class)))
                .thenReturn(Flux.just(top1, top2));

        StepVerifier.create(service.topProductos(5))
                .assertNext(t -> {
                    assertThat(t.getProducto()).isEqualTo("Portátil gaming");
                    assertThat(t.getTotalImporte()).isEqualTo(12598.75);
                })
                .assertNext(t -> assertThat(t.getProducto()).isEqualTo("Monitor 4K"))
                .verifyComplete();
    }

    @Test
    void topProductos_sinVentas_retornaFluxVacio() {
        when(reactiveMongoTemplate.aggregate(any(Aggregation.class), eq("ventas"), eq(TopProductoDTO.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.topProductos(3))
                .verifyComplete();
    }
}
