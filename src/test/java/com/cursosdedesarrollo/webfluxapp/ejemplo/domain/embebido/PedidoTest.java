package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_lineasInicializadasVacias() {
        Pedido p = new Pedido();
        assertThat(p.getId()).isNull();
        assertThat(p.getCliente()).isNull();
        assertThat(p.getFecha()).isNull();
        assertThat(p.getEstado()).isNull();
        assertThat(p.getLineas()).isNotNull().isEmpty();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        List<LineaPedido> lineas = List.of(new LineaPedido("Teclado", 2, 89.99));
        Pedido p = new Pedido("p1", "David", LocalDate.of(2024, 1, 15), "PENDIENTE", new ArrayList<>(lineas));
        assertThat(p.getId()).isEqualTo("p1");
        assertThat(p.getCliente()).isEqualTo("David");
        assertThat(p.getEstado()).isEqualTo("PENDIENTE");
        assertThat(p.getLineas()).hasSize(1);
    }

    @Test
    void settersYGetters_funcionan() {
        Pedido p = new Pedido();
        p.setId("p1");
        p.setCliente("Ana");
        p.setFecha(LocalDate.of(2024, 6, 1));
        p.setEstado("ENVIADO");
        assertThat(p.getCliente()).isEqualTo("Ana");
        assertThat(p.getEstado()).isEqualTo("ENVIADO");
    }

    @Test
    void equals_dosPedidosConMismosDatos_sonIguales() {
        Pedido p1 = new Pedido("p1", "David", LocalDate.of(2024, 1, 1), "PENDIENTE", new ArrayList<>());
        Pedido p2 = new Pedido("p1", "David", LocalDate.of(2024, 1, 1), "PENDIENTE", new ArrayList<>());
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Pedido p = new Pedido("p1", "David", LocalDate.of(2024, 1, 1), "PENDIENTE", new ArrayList<>());
        assertThat(p.toString()).contains("David", "PENDIENTE");
    }

    @Test
    void agregarLineaALista_incrementaTamano() {
        Pedido p = new Pedido();
        p.getLineas().add(new LineaPedido("Teclado", 1, 89.99));
        p.getLineas().add(new LineaPedido("Ratón", 2, 29.99));
        assertThat(p.getLineas()).hasSize(2);
    }

    @Test
    void validacion_pedidoValido_sinViolaciones() {
        Pedido p = new Pedido(null, "David", LocalDate.now(), "PENDIENTE", new ArrayList<>());
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test
    void validacion_clienteBlanco_generaViolacion() {
        Pedido p = new Pedido(null, "   ", LocalDate.now(), "PENDIENTE", new ArrayList<>());
        Set<ConstraintViolation<Pedido>> violations = validator.validate(p);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("cliente");
    }

    @Test
    void validacion_estadoBlanco_generaViolacion() {
        Pedido p = new Pedido(null, "David", LocalDate.now(), "", new ArrayList<>());
        Set<ConstraintViolation<Pedido>> violations = validator.validate(p);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("estado");
    }

    @Test
    void validacion_fechaNula_generaViolacion() {
        Pedido p = new Pedido(null, "David", null, "PENDIENTE", new ArrayList<>());
        Set<ConstraintViolation<Pedido>> violations = validator.validate(p);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("fecha");
    }
}
