package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmpleadoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Empleado e = new Empleado();
        assertThat(e.getId()).isNull();
        assertThat(e.getNombre()).isNull();
        assertThat(e.getPuesto()).isNull();
        assertThat(e.getContratoId()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Empleado e = new Empleado("1", "Ana García", "Desarrolladora", "c1");
        assertThat(e.getId()).isEqualTo("1");
        assertThat(e.getNombre()).isEqualTo("Ana García");
        assertThat(e.getPuesto()).isEqualTo("Desarrolladora");
        assertThat(e.getContratoId()).isEqualTo("c1");
    }

    @Test
    void settersYGetters_funcionan() {
        Empleado e = new Empleado();
        e.setId("2");
        e.setNombre("Luis");
        e.setPuesto("QA");
        e.setContratoId("c2");
        assertThat(e.getNombre()).isEqualTo("Luis");
        assertThat(e.getPuesto()).isEqualTo("QA");
        assertThat(e.getContratoId()).isEqualTo("c2");
    }

    @Test
    void equals_dosEmpleadosConMismosDatos_sonIguales() {
        Empleado e1 = new Empleado("1", "Ana", "Dev", null);
        Empleado e2 = new Empleado("1", "Ana", "Dev", null);
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Empleado e = new Empleado("1", "Ana", "Dev", "c1");
        assertThat(e.toString()).contains("Ana", "Dev", "c1");
    }

    @Test
    void validacion_empleadoValido_sinViolaciones() {
        Empleado e = new Empleado(null, "Ana García", "Desarrolladora", null);
        assertThat(validator.validate(e)).isEmpty();
    }

    @Test
    void validacion_nombreBlanco_generaViolacion() {
        Empleado e = new Empleado(null, "   ", "Desarrolladora", null);
        Set<ConstraintViolation<Empleado>> violations = validator.validate(e);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("nombre");
    }

    @Test
    void validacion_puestoBlanco_generaViolacion() {
        Empleado e = new Empleado(null, "Ana", "   ", null);
        Set<ConstraintViolation<Empleado>> violations = validator.validate(e);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("puesto");
    }

    @Test
    void validacion_contratoIdNulo_esValido() {
        Empleado e = new Empleado(null, "Ana", "Dev", null);
        assertThat(validator.validate(e)).isEmpty();
    }
}
