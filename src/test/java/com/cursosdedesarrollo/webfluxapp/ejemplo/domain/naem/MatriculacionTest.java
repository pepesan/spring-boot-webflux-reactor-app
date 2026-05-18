package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MatriculacionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Matriculacion m = new Matriculacion();
        assertThat(m.getId()).isNull();
        assertThat(m.getEstudianteId()).isNull();
        assertThat(m.getCursoId()).isNull();
        assertThat(m.getFechaAlta()).isNull();
        assertThat(m.getNota()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        LocalDate hoy = LocalDate.of(2024, 9, 1);
        Matriculacion m = new Matriculacion("m1", "e1", "c1", hoy, 8.5);
        assertThat(m.getId()).isEqualTo("m1");
        assertThat(m.getEstudianteId()).isEqualTo("e1");
        assertThat(m.getCursoId()).isEqualTo("c1");
        assertThat(m.getFechaAlta()).isEqualTo(hoy);
        assertThat(m.getNota()).isEqualTo(8.5);
    }

    @Test
    void setNota_actualizaElCampo() {
        Matriculacion m = new Matriculacion("m1", "e1", "c1", LocalDate.now(), null);
        m.setNota(9.0);
        assertThat(m.getNota()).isEqualTo(9.0);
    }

    @Test
    void equals_dosMatriculacionesConMismosDatos_sonIguales() {
        LocalDate fecha = LocalDate.of(2024, 9, 1);
        Matriculacion m1 = new Matriculacion("m1", "e1", "c1", fecha, null);
        Matriculacion m2 = new Matriculacion("m1", "e1", "c1", fecha, null);
        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Matriculacion m = new Matriculacion("m1", "e1", "c1", LocalDate.of(2024, 9, 1), 7.5);
        assertThat(m.toString()).contains("e1", "c1", "7.5");
    }

    @Test
    void validacion_matriculacionValida_sinViolaciones() {
        Matriculacion m = new Matriculacion(null, "e1", "c1", LocalDate.now(), null);
        assertThat(validator.validate(m)).isEmpty();
    }

    @Test
    void validacion_estudianteIdBlanco_generaViolacion() {
        Matriculacion m = new Matriculacion(null, "   ", "c1", LocalDate.now(), null);
        Set<ConstraintViolation<Matriculacion>> violations = validator.validate(m);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("estudianteId");
    }

    @Test
    void validacion_cursoIdBlanco_generaViolacion() {
        Matriculacion m = new Matriculacion(null, "e1", "   ", LocalDate.now(), null);
        Set<ConstraintViolation<Matriculacion>> violations = validator.validate(m);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("cursoId");
    }

    @Test
    void validacion_fechaAltaNula_generaViolacion() {
        Matriculacion m = new Matriculacion(null, "e1", "c1", null, null);
        Set<ConstraintViolation<Matriculacion>> violations = validator.validate(m);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("fechaAlta");
    }

    @Test
    void validacion_notaNulaEsValido() {
        Matriculacion m = new Matriculacion(null, "e1", "c1", LocalDate.now(), null);
        assertThat(validator.validate(m)).isEmpty();
    }
}
