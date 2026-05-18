package com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoauno.EmpleadoConContratoDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno.ContratoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    // ── CRUD Empleado ────────────────────────────────────────────────────────

    public Flux<Empleado> findAllEmpleados() {
        return empleadoRepository.findAll();
    }

    public Mono<Empleado> createEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    public Mono<ResponseEntity<Empleado>> deleteEmpleado(String id) {
        return empleadoRepository.findById(id)
                .flatMap(e -> empleadoRepository.delete(e).then(Mono.just(ResponseEntity.ok(e))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ── CRUD Contrato ────────────────────────────────────────────────────────

    public Flux<Contrato> findAllContratos() {
        return contratoRepository.findAll();
    }

    public Mono<ResponseEntity<Contrato>> getContrato(String id) {
        return contratoRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<Contrato> createContrato(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public Mono<ResponseEntity<Contrato>> deleteContrato(String id) {
        return contratoRepository.findById(id)
                .flatMap(c -> contratoRepository.delete(c).then(Mono.just(ResponseEntity.ok(c))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ── Operaciones 1:1 ──────────────────────────────────────────────────────

    /**
     * Dos queries encadenadas con flatMap: busca el empleado y luego resuelve
     * su contrato por contratoId. Si el empleado no tiene contrato asignado
     * devuelve el DTO con contrato=null.
     */
    public Mono<EmpleadoConContratoDTO> findWithContrato(String id) {
        return empleadoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado")))
                .flatMap(empleado -> {
                    if (empleado.getContratoId() == null) {
                        return Mono.just(new EmpleadoConContratoDTO(empleado, null));
                    }
                    return contratoRepository.findById(empleado.getContratoId())
                            .map(contrato -> new EmpleadoConContratoDTO(empleado, contrato))
                            .defaultIfEmpty(new EmpleadoConContratoDTO(empleado, null));
                });
    }

    /**
     * Guarda el contrato primero (obtiene el ID generado por MongoDB),
     * luego guarda el empleado con ese ID referenciado.
     * Demuestra flatMap de escritura encadenada.
     */
    public Mono<EmpleadoConContratoDTO> crearConContrato(Empleado empleado, Contrato contrato) {
        return contratoRepository.save(contrato)
                .flatMap(contratoGuardado -> {
                    empleado.setContratoId(contratoGuardado.getId());
                    return empleadoRepository.save(empleado)
                            .map(empleadoGuardado -> new EmpleadoConContratoDTO(empleadoGuardado, contratoGuardado));
                });
    }

    /**
     * Recupera empleado y contrato en paralelo con zipWith,
     * luego actualiza la referencia contratoId en el empleado.
     */
    public Mono<EmpleadoConContratoDTO> asignarContrato(String empleadoId, String contratoId) {
        Mono<Empleado> empleadoMono = empleadoRepository.findById(empleadoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado")));
        Mono<Contrato> contratoMono = contratoRepository.findById(contratoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato no encontrado")));

        return empleadoMono.zipWith(contratoMono)
                .flatMap(tuple -> {
                    Empleado empleado = tuple.getT1();
                    Contrato contrato = tuple.getT2();
                    empleado.setContratoId(contrato.getId());
                    return empleadoRepository.save(empleado)
                            .map(guardado -> new EmpleadoConContratoDTO(guardado, contrato));
                });
    }
}
