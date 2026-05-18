package com.cursosdedesarrollo.webfluxapp.ejercicios;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Getters y setters ────────────────────────────────────────────────────

    @Test
    void settersYGetters_funcionanCorrectamente() {
        Cliente c = new Cliente();
        c.setId("1");
        c.setNombre("Empresa ABC");
        c.setDireccion("Calle Mayor 1");
        c.setTlf("600000000");
        c.setCorreo("abc@empresa.com");

        assertThat(c.getId()).isEqualTo("1");
        assertThat(c.getNombre()).isEqualTo("Empresa ABC");
        assertThat(c.getDireccion()).isEqualTo("Calle Mayor 1");
        assertThat(c.getTlf()).isEqualTo("600000000");
        assertThat(c.getCorreo()).isEqualTo("abc@empresa.com");
    }

    // ── equals y hashCode ────────────────────────────────────────────────────

    @Test
    void equals_dosClientesConMismosDatos_sonIguales() {
        Cliente c1 = cliente("1", "Empresa ABC");
        Cliente c2 = cliente("1", "Empresa ABC");
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void equals_clientesConDistintoId_noSonIguales() {
        Cliente c1 = cliente("1", "Empresa ABC");
        Cliente c2 = cliente("2", "Empresa ABC");
        assertThat(c1).isNotEqualTo(c2);
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_contieneLosCampos() {
        Cliente c = cliente("1", "Empresa ABC");
        assertThat(c.toString()).contains("1", "Empresa ABC");
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    @Test
    void validacion_clienteValido_sinViolaciones() {
        assertThat(validator.validate(cliente("1", "Empresa ABC"))).isEmpty();
    }

    @Test
    void validacion_nombreNulo_generaViolacion() {
        Cliente c = cliente("1", null);
        Set<ConstraintViolation<Cliente>> violations = validator.validate(c);
        // @NotNull y @NotBlank disparan ambas con null → containsOnly ignora duplicados
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsOnly("nombre");
    }

    @Test
    void validacion_nombreVacio_generaViolacion() {
        Cliente c = cliente("1", "");
        Set<ConstraintViolation<Cliente>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("nombre");
    }

    @Test
    void validacion_nombreSoloEspacios_generaViolacion() {
        Cliente c = cliente("1", "   ");
        Set<ConstraintViolation<Cliente>> violations = validator.validate(c);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("nombre");
    }

    @Test
    void validacion_camposOpcionalesNulos_sonValidos() {
        Cliente c = new Cliente();
        c.setNombre("Empresa ABC");
        assertThat(validator.validate(c)).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Cliente cliente(String id, String nombre) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }
}
