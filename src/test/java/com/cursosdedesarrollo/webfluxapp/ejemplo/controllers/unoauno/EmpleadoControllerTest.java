package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno.EmpleadoConContratoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoauno.EmpleadoService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class EmpleadoControllerTest {

    @Mock
    private EmpleadoService empleadoService;

    @InjectMocks
    private EmpleadoController controller;

    private WebTestClient client;
    private Empleado empleado;
    private Contrato contrato;
    private EmpleadoConContratoDTO dto;

    @BeforeEach
    void setUp() {
        contrato = new Contrato("c1", "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);
        empleado = new Empleado("e1", "Ana García", "Desarrolladora", "c1");
        dto = new EmpleadoConContratoDTO(empleado, contrato);

        client = MockMvcWebTestClient.bindToController(controller).build();
    }

    // ── Empleados ────────────────────────────────────────────────────────────

    @Test
    void getAllEmpleados_retornaLista() {
        when(empleadoService.findAllEmpleados()).thenReturn(Flux.just(empleado));

        client.get().uri("/api/relaciones/1a1/empleados")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Empleado.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getNombre()).isEqualTo("Ana García"));
    }

    @Test
    void createEmpleado_retorna201() {
        when(empleadoService.createEmpleado(any(Empleado.class))).thenReturn(Mono.just(empleado));

        client.post().uri("/api/relaciones/1a1/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Ana García\",\"puesto\":\"Desarrolladora\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Empleado.class)
                .value(e -> assertThat(e.getNombre()).isEqualTo("Ana García"));
    }

    @Test
    void getEmpleadoConContrato_encontrado_retornaDTOCompleto() {
        when(empleadoService.findWithContrato("e1")).thenReturn(Mono.just(dto));

        client.get().uri("/api/relaciones/1a1/empleados/e1/completo")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmpleadoConContratoDTO.class)
                .value(d -> {
                    assertThat(d.empleado().getId()).isEqualTo("e1");
                    assertThat(d.contrato().getTipo()).isEqualTo("INDEFINIDO");
                });
    }

    @Test
    void getEmpleadoConContrato_noEncontrado_retorna404() {
        when(empleadoService.findWithContrato("nope"))
                .thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND, "Empleado no encontrado")));

        client.get().uri("/api/relaciones/1a1/empleados/nope/completo")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void asignarContrato_retornaDTOActualizado() {
        when(empleadoService.asignarContrato("e1", "c1")).thenReturn(Mono.just(dto));

        client.put().uri("/api/relaciones/1a1/empleados/e1/contrato/c1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmpleadoConContratoDTO.class)
                .value(d -> assertThat(d.empleado().getContratoId()).isEqualTo("c1"));
    }

    @Test
    void deleteEmpleado_encontrado_retorna200() {
        when(empleadoService.deleteEmpleado("e1")).thenReturn(Mono.just(ResponseEntity.ok(empleado)));

        client.delete().uri("/api/relaciones/1a1/empleados/e1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Empleado.class)
                .value(e -> assertThat(e.getId()).isEqualTo("e1"));
    }

    @Test
    void deleteEmpleado_noEncontrado_retorna404() {
        when(empleadoService.deleteEmpleado("nope"))
                .thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.delete().uri("/api/relaciones/1a1/empleados/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── Contratos ────────────────────────────────────────────────────────────

    @Test
    void getAllContratos_retornaLista() {
        when(empleadoService.findAllContratos()).thenReturn(Flux.just(contrato));

        client.get().uri("/api/relaciones/1a1/contratos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Contrato.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getTipo()).isEqualTo("INDEFINIDO"));
    }

    @Test
    void getContrato_encontrado_retorna200() {
        when(empleadoService.getContrato("c1")).thenReturn(Mono.just(ResponseEntity.ok(contrato)));

        client.get().uri("/api/relaciones/1a1/contratos/c1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Contrato.class)
                .value(c -> assertThat(c.getSalario()).isEqualTo(35000.0));
    }

    @Test
    void getContrato_noEncontrado_retorna404() {
        when(empleadoService.getContrato("nope"))
                .thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.get().uri("/api/relaciones/1a1/contratos/nope")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createContrato_retorna201() {
        when(empleadoService.createContrato(any(Contrato.class))).thenReturn(Mono.just(contrato));

        client.post().uri("/api/relaciones/1a1/contratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"tipo\":\"INDEFINIDO\",\"fechaInicio\":\"2024-01-01\",\"salario\":35000.0}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Contrato.class)
                .value(c -> assertThat(c.getTipo()).isEqualTo("INDEFINIDO"));
    }

    @Test
    void deleteContrato_encontrado_retorna200() {
        when(empleadoService.deleteContrato("c1")).thenReturn(Mono.just(ResponseEntity.ok(contrato)));

        client.delete().uri("/api/relaciones/1a1/contratos/c1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Contrato.class)
                .value(c -> assertThat(c.getId()).isEqualTo("c1"));
    }

    @Test
    void deleteContrato_noEncontrado_retorna404() {
        when(empleadoService.deleteContrato("nope"))
                .thenReturn(Mono.just(ResponseEntity.notFound().build()));

        client.delete().uri("/api/relaciones/1a1/contratos/nope")
                .exchange()
                .expectStatus().isNotFound();
    }
}
