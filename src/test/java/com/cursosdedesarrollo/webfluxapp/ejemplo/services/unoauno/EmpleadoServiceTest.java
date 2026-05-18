package com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno.EmpleadoConContratoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno.ContratoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private EmpleadoService service;

    private Empleado empleado;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        contrato = new Contrato("c1", "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);
        empleado = new Empleado("e1", "Ana García", "Desarrolladora", "c1");
    }

    // ── findWithContrato ──────────────────────────────────────────────────────

    @Test
    void findWithContrato_empleadoConContrato_resuelveAmbosPorFlatMap() {
        when(empleadoRepository.findById("e1")).thenReturn(Mono.just(empleado));
        when(contratoRepository.findById("c1")).thenReturn(Mono.just(contrato));

        StepVerifier.create(service.findWithContrato("e1"))
                .assertNext(dto -> {
                    assertThat(dto.empleado()).isEqualTo(empleado);
                    assertThat(dto.contrato()).isEqualTo(contrato);
                })
                .verifyComplete();
    }

    @Test
    void findWithContrato_empleadoSinContratoId_retornaDTOConContratoNull() {
        empleado.setContratoId(null);
        when(empleadoRepository.findById("e1")).thenReturn(Mono.just(empleado));

        StepVerifier.create(service.findWithContrato("e1"))
                .assertNext(dto -> {
                    assertThat(dto.empleado()).isEqualTo(empleado);
                    assertThat(dto.contrato()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void findWithContrato_contratoIdSetPeroContratoNoExiste_retornaDTOConContratoNull() {
        when(empleadoRepository.findById("e1")).thenReturn(Mono.just(empleado));
        when(contratoRepository.findById("c1")).thenReturn(Mono.empty());

        StepVerifier.create(service.findWithContrato("e1"))
                .assertNext(dto -> assertThat(dto.contrato()).isNull())
                .verifyComplete();
    }

    @Test
    void findWithContrato_empleadoNoExiste_lanza404() {
        when(empleadoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.findWithContrato("nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    // ── crearConContrato ──────────────────────────────────────────────────────

    @Test
    void crearConContrato_guardaContratoAntesQueEmpleadoYVincula() {
        Empleado sinId = new Empleado(null, "Ana García", "Desarrolladora", null);
        Contrato sinId2 = new Contrato(null, "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);

        when(contratoRepository.save(sinId2)).thenReturn(Mono.just(contrato));
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(Mono.just(empleado));

        StepVerifier.create(service.crearConContrato(sinId, sinId2))
                .assertNext(dto -> {
                    assertThat(dto.contrato().getId()).isEqualTo("c1");
                    assertThat(dto.empleado().getContratoId()).isEqualTo("c1");
                })
                .verifyComplete();
    }

    // ── asignarContrato ───────────────────────────────────────────────────────

    @Test
    void asignarContrato_ambosPresentesZipWithYActualiza() {
        Empleado sinContrato = new Empleado("e1", "Ana García", "Desarrolladora", null);
        when(empleadoRepository.findById("e1")).thenReturn(Mono.just(sinContrato));
        when(contratoRepository.findById("c1")).thenReturn(Mono.just(contrato));
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(Mono.just(empleado));

        StepVerifier.create(service.asignarContrato("e1", "c1"))
                .assertNext(dto -> {
                    assertThat(dto.empleado().getContratoId()).isEqualTo("c1");
                    assertThat(dto.contrato()).isEqualTo(contrato);
                })
                .verifyComplete();
    }

    @Test
    void asignarContrato_empleadoNoExiste_lanza404() {
        when(empleadoRepository.findById("nope")).thenReturn(Mono.empty());
        when(contratoRepository.findById("c1")).thenReturn(Mono.just(contrato));

        StepVerifier.create(service.asignarContrato("nope", "c1"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void asignarContrato_contratoNoExiste_lanza404() {
        when(empleadoRepository.findById("e1")).thenReturn(Mono.just(empleado));
        when(contratoRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(service.asignarContrato("e1", "nope"))
                .expectErrorMatches(e -> e instanceof ResponseStatusException
                        && ((ResponseStatusException) e).getStatusCode().value() == 404)
                .verify();
    }
}
