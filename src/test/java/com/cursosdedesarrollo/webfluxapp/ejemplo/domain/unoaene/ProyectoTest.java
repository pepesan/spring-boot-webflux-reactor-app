package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProyectoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Proyecto p = new Proyecto();
        assertThat(p.getId()).isNull();
        assertThat(p.getNombre()).isNull();
        assertThat(p.getDescripcion()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Proyecto p = new Proyecto("1", "Portal de clientes", "Migración del portal legacy");
        assertThat(p.getId()).isEqualTo("1");
        assertThat(p.getNombre()).isEqualTo("Portal de clientes");
        assertThat(p.getDescripcion()).isEqualTo("Migración del portal legacy");
    }

    @Test
    void settersYGetters_funcionan() {
        Proyecto p = new Proyecto();
        p.setNombre("API Gateway");
        p.setDescripcion("Centraliza las llamadas externas");
        assertThat(p.getNombre()).isEqualTo("API Gateway");
        assertThat(p.getDescripcion()).isEqualTo("Centraliza las llamadas externas");
    }

    @Test
    void equals_dosProyectosConMismosDatos_sonIguales() {
        Proyecto p1 = new Proyecto("1", "Portal", null);
        Proyecto p2 = new Proyecto("1", "Portal", null);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Proyecto p = new Proyecto("1", "Portal", "Desc");
        assertThat(p.toString()).contains("Portal", "Desc");
    }

    @Test
    void validacion_proyectoValido_sinViolaciones() {
        Proyecto p = new Proyecto(null, "Portal de clientes", null);
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test
    void validacion_nombreBlanco_generaViolacion() {
        Proyecto p = new Proyecto(null, "   ", null);
        Set<ConstraintViolation<Proyecto>> violations = validator.validate(p);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("nombre");
    }

    @Test
    void validacion_descripcionNula_esValido() {
        Proyecto p = new Proyecto(null, "Portal", null);
        assertThat(validator.validate(p)).isEmpty();
    }
}
