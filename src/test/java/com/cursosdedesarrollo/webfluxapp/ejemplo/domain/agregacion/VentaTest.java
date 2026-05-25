package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VentaTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Venta v = new Venta();
        assertThat(v.getId()).isNull();
        assertThat(v.getProducto()).isNull();
        assertThat(v.getCategoria()).isNull();
        assertThat(v.getImporte()).isNull();
        assertThat(v.getFecha()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Venta v = new Venta("v1", "Portátil gaming", "ELECTRONICA", 1299.99, LocalDate.of(2024, 1, 15));
        assertThat(v.getId()).isEqualTo("v1");
        assertThat(v.getProducto()).isEqualTo("Portátil gaming");
        assertThat(v.getCategoria()).isEqualTo("ELECTRONICA");
        assertThat(v.getImporte()).isEqualTo(1299.99);
    }

    @Test
    void settersYGetters_funcionan() {
        Venta v = new Venta();
        v.setId("v1");
        v.setProducto("Zapatillas");
        v.setCategoria("DEPORTE");
        v.setImporte(89.99);
        v.setFecha(LocalDate.of(2024, 3, 10));
        assertThat(v.getProducto()).isEqualTo("Zapatillas");
        assertThat(v.getCategoria()).isEqualTo("DEPORTE");
    }

    @Test
    void equals_dosVentasConMismosDatos_sonIguales() {
        Venta v1 = new Venta("v1", "Portátil", "ELECTRONICA", 999.0, LocalDate.of(2024, 1, 1));
        Venta v2 = new Venta("v1", "Portátil", "ELECTRONICA", 999.0, LocalDate.of(2024, 1, 1));
        assertThat(v1).isEqualTo(v2);
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Venta v = new Venta("v1", "Portátil", "ELECTRONICA", 999.0, LocalDate.of(2024, 1, 1));
        assertThat(v.toString()).contains("Portátil", "ELECTRONICA");
    }

    @Test
    void validacion_ventaValida_sinViolaciones() {
        Venta v = new Venta(null, "Portátil", "ELECTRONICA", 999.0, LocalDate.now());
        assertThat(validator.validate(v)).isEmpty();
    }

    @Test
    void validacion_productoBlanco_generaViolacion() {
        Venta v = new Venta(null, "   ", "ELECTRONICA", 999.0, LocalDate.now());
        Set<ConstraintViolation<Venta>> violations = validator.validate(v);
        assertThat(violations).extracting(v2 -> v2.getPropertyPath().toString())
                .containsExactly("producto");
    }

    @Test
    void validacion_categoriaBlanca_generaViolacion() {
        Venta v = new Venta(null, "Portátil", "", 999.0, LocalDate.now());
        Set<ConstraintViolation<Venta>> violations = validator.validate(v);
        assertThat(violations).extracting(v2 -> v2.getPropertyPath().toString())
                .containsExactly("categoria");
    }

    @Test
    void validacion_importeNulo_generaViolacion() {
        Venta v = new Venta(null, "Portátil", "ELECTRONICA", null, LocalDate.now());
        Set<ConstraintViolation<Venta>> violations = validator.validate(v);
        assertThat(violations).extracting(v2 -> v2.getPropertyPath().toString())
                .containsExactly("importe");
    }

    @Test
    void validacion_fechaNula_generaViolacion() {
        Venta v = new Venta(null, "Portátil", "ELECTRONICA", 999.0, null);
        Set<ConstraintViolation<Venta>> violations = validator.validate(v);
        assertThat(violations).extracting(v2 -> v2.getPropertyPath().toString())
                .containsExactly("fecha");
    }
}
