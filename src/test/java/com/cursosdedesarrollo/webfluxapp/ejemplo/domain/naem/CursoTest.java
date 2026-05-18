package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CursoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Curso c = new Curso();
        assertThat(c.getId()).isNull();
        assertThat(c.getTitulo()).isNull();
        assertThat(c.getDescripcion()).isNull();
        assertThat(c.getPlazas()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Curso c = new Curso("c1", "Spring WebFlux", "Programación reactiva", 30);
        assertThat(c.getId()).isEqualTo("c1");
        assertThat(c.getTitulo()).isEqualTo("Spring WebFlux");
        assertThat(c.getDescripcion()).isEqualTo("Programación reactiva");
        assertThat(c.getPlazas()).isEqualTo(30);
    }

    @Test
    void settersYGetters_funcionan() {
        Curso c = new Curso();
        c.setId("c2");
        c.setTitulo("Docker");
        c.setDescripcion("Contenedores");
        c.setPlazas(20);
        assertThat(c.getTitulo()).isEqualTo("Docker");
        assertThat(c.getPlazas()).isEqualTo(20);
    }

    @Test
    void equals_dosCursosConMismosDatos_sonIguales() {
        Curso c1 = new Curso("c1", "Spring WebFlux", null, 30);
        Curso c2 = new Curso("c1", "Spring WebFlux", null, 30);
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Curso c = new Curso("c1", "Spring WebFlux", "Reactivo", 30);
        assertThat(c.toString()).contains("Spring WebFlux", "30");
    }

    @Test
    void validacion_cursoValido_sinViolaciones() {
        Curso c = new Curso(null, "Spring WebFlux", null, 30);
        assertThat(validator.validate(c)).isEmpty();
    }

    @Test
    void validacion_tituloBlanco_generaViolacion() {
        Curso c = new Curso(null, "   ", null, 30);
        Set<ConstraintViolation<Curso>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("titulo");
    }

    @Test
    void validacion_plazasNulas_generaViolacion() {
        Curso c = new Curso(null, "Spring WebFlux", null, null);
        Set<ConstraintViolation<Curso>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("plazas");
    }

    @Test
    void validacion_plazasCero_generaViolacion() {
        Curso c = new Curso(null, "Spring WebFlux", null, 0);
        Set<ConstraintViolation<Curso>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("plazas");
    }

    @Test
    void validacion_descripcionNula_esValido() {
        Curso c = new Curso(null, "Spring WebFlux", null, 30);
        assertThat(validator.validate(c)).isEmpty();
    }
}
