package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TareaTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Tarea t = new Tarea();
        assertThat(t.getId()).isNull();
        assertThat(t.getTitulo()).isNull();
        assertThat(t.getEstado()).isNull();
        assertThat(t.getProyectoId()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Tarea t = new Tarea("t1", "Diseñar login", "PENDIENTE", "p1");
        assertThat(t.getId()).isEqualTo("t1");
        assertThat(t.getTitulo()).isEqualTo("Diseñar login");
        assertThat(t.getEstado()).isEqualTo("PENDIENTE");
        assertThat(t.getProyectoId()).isEqualTo("p1");
    }

    @Test
    void settersYGetters_funcionan() {
        Tarea t = new Tarea();
        t.setTitulo("Implementar API");
        t.setEstado("EN_CURSO");
        t.setProyectoId("p2");
        assertThat(t.getTitulo()).isEqualTo("Implementar API");
        assertThat(t.getEstado()).isEqualTo("EN_CURSO");
        assertThat(t.getProyectoId()).isEqualTo("p2");
    }

    @Test
    void equals_dosTareasConMismosDatos_sonIguales() {
        Tarea t1 = new Tarea("t1", "Login", "PENDIENTE", "p1");
        Tarea t2 = new Tarea("t1", "Login", "PENDIENTE", "p1");
        assertThat(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Tarea t = new Tarea("t1", "Login", "HECHO", "p1");
        assertThat(t.toString()).contains("Login", "HECHO", "p1");
    }

    @Test
    void validacion_tareaValida_sinViolaciones() {
        Tarea t = new Tarea(null, "Diseñar login", "PENDIENTE", "p1");
        assertThat(validator.validate(t)).isEmpty();
    }

    @Test
    void validacion_tituloBlanco_generaViolacion() {
        Tarea t = new Tarea(null, "  ", "PENDIENTE", "p1");
        Set<ConstraintViolation<Tarea>> violations = validator.validate(t);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("titulo");
    }

    @Test
    void validacion_estadoBlanco_generaViolacion() {
        Tarea t = new Tarea(null, "Login", "  ", "p1");
        Set<ConstraintViolation<Tarea>> violations = validator.validate(t);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("estado");
    }

    @Test
    void validacion_proyectoIdBlanco_generaViolacion() {
        Tarea t = new Tarea(null, "Login", "PENDIENTE", "  ");
        Set<ConstraintViolation<Tarea>> violations = validator.validate(t);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("proyectoId");
    }
}
