package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EstudianteTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Estudiante e = new Estudiante();
        assertThat(e.getId()).isNull();
        assertThat(e.getNombre()).isNull();
        assertThat(e.getEmail()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Estudiante e = new Estudiante("e1", "Laura Martínez", "laura@ejemplo.com");
        assertThat(e.getId()).isEqualTo("e1");
        assertThat(e.getNombre()).isEqualTo("Laura Martínez");
        assertThat(e.getEmail()).isEqualTo("laura@ejemplo.com");
    }

    @Test
    void settersYGetters_funcionan() {
        Estudiante e = new Estudiante();
        e.setId("e2");
        e.setNombre("Carlos");
        e.setEmail("carlos@test.com");
        assertThat(e.getNombre()).isEqualTo("Carlos");
        assertThat(e.getEmail()).isEqualTo("carlos@test.com");
    }

    @Test
    void equals_dosEstudiantesConMismosDatos_sonIguales() {
        Estudiante e1 = new Estudiante("e1", "Laura", "laura@ejemplo.com");
        Estudiante e2 = new Estudiante("e1", "Laura", "laura@ejemplo.com");
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Estudiante e = new Estudiante("e1", "Laura", "laura@ejemplo.com");
        assertThat(e.toString()).contains("Laura", "laura@ejemplo.com");
    }

    @Test
    void validacion_estudianteValido_sinViolaciones() {
        Estudiante e = new Estudiante(null, "Laura Martínez", "laura@ejemplo.com");
        assertThat(validator.validate(e)).isEmpty();
    }

    @Test
    void validacion_nombreBlanco_generaViolacion() {
        Estudiante e = new Estudiante(null, "   ", "laura@ejemplo.com");
        Set<ConstraintViolation<Estudiante>> violations = validator.validate(e);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("nombre");
    }

    @Test
    void validacion_emailFormatoInvalido_generaViolacion() {
        Estudiante e = new Estudiante(null, "Laura", "no-es-un-email");
        Set<ConstraintViolation<Estudiante>> violations = validator.validate(e);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void validacion_emailBlanco_generaDosViolaciones() {
        Estudiante e = new Estudiante(null, "Laura", "   ");
        Set<ConstraintViolation<Estudiante>> violations = validator.validate(e);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "email");
    }
}
