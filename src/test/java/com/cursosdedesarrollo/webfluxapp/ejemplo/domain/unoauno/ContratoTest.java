package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ContratoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Contrato c = new Contrato();
        assertThat(c.getId()).isNull();
        assertThat(c.getTipo()).isNull();
        assertThat(c.getFechaInicio()).isNull();
        assertThat(c.getFechaFin()).isNull();
        assertThat(c.getSalario()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);
        Contrato c = new Contrato("1", "TEMPORAL", inicio, fin, 30000.0);
        assertThat(c.getTipo()).isEqualTo("TEMPORAL");
        assertThat(c.getFechaInicio()).isEqualTo(inicio);
        assertThat(c.getFechaFin()).isEqualTo(fin);
        assertThat(c.getSalario()).isEqualTo(30000.0);
    }

    @Test
    void settersYGetters_funcionan() {
        Contrato c = new Contrato();
        c.setTipo("INDEFINIDO");
        c.setFechaInicio(LocalDate.of(2023, 6, 1));
        c.setSalario(40000.0);
        assertThat(c.getTipo()).isEqualTo("INDEFINIDO");
        assertThat(c.getSalario()).isEqualTo(40000.0);
    }

    @Test
    void equals_dosContratosConMismosDatos_sonIguales() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        Contrato c1 = new Contrato("1", "INDEFINIDO", inicio, null, 35000.0);
        Contrato c2 = new Contrato("1", "INDEFINIDO", inicio, null, 35000.0);
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Contrato c = new Contrato("1", "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);
        assertThat(c.toString()).contains("INDEFINIDO", "35000.0");
    }

    @Test
    void validacion_contratoValido_sinViolaciones() {
        Contrato c = new Contrato(null, "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);
        assertThat(validator.validate(c)).isEmpty();
    }

    @Test
    void validacion_tipoBlanco_generaViolacion() {
        Contrato c = new Contrato(null, "  ", LocalDate.of(2024, 1, 1), null, 35000.0);
        Set<ConstraintViolation<Contrato>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("tipo");
    }

    @Test
    void validacion_salarioNulo_generaViolacion() {
        Contrato c = new Contrato(null, "INDEFINIDO", LocalDate.of(2024, 1, 1), null, null);
        Set<ConstraintViolation<Contrato>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("salario");
    }

    @Test
    void validacion_fechaInicioNula_generaViolacion() {
        Contrato c = new Contrato(null, "INDEFINIDO", null, null, 35000.0);
        Set<ConstraintViolation<Contrato>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("fechaInicio");
    }

    @Test
    void validacion_fechaFinNula_esValido() {
        Contrato c = new Contrato(null, "INDEFINIDO", LocalDate.of(2024, 1, 1), null, 35000.0);
        assertThat(validator.validate(c)).isEmpty();
    }
}
