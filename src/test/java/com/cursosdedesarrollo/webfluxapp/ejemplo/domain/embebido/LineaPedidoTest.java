package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.embebido;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LineaPedidoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        LineaPedido l = new LineaPedido();
        assertThat(l.getProducto()).isNull();
        assertThat(l.getCantidad()).isNull();
        assertThat(l.getPrecioUnitario()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        LineaPedido l = new LineaPedido("Teclado mecánico", 2, 89.99);
        assertThat(l.getProducto()).isEqualTo("Teclado mecánico");
        assertThat(l.getCantidad()).isEqualTo(2);
        assertThat(l.getPrecioUnitario()).isEqualTo(89.99);
    }

    @Test
    void settersYGetters_funcionan() {
        LineaPedido l = new LineaPedido();
        l.setProducto("Ratón");
        l.setCantidad(1);
        l.setPrecioUnitario(29.99);
        assertThat(l.getProducto()).isEqualTo("Ratón");
        assertThat(l.getCantidad()).isEqualTo(1);
        assertThat(l.getPrecioUnitario()).isEqualTo(29.99);
    }

    @Test
    void equals_dosLineasConMismosDatos_sonIguales() {
        LineaPedido l1 = new LineaPedido("Teclado", 2, 89.99);
        LineaPedido l2 = new LineaPedido("Teclado", 2, 89.99);
        assertThat(l1).isEqualTo(l2);
        assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        LineaPedido l = new LineaPedido("Teclado", 2, 89.99);
        assertThat(l.toString()).contains("Teclado", "2", "89.99");
    }

    @Test
    void validacion_lineaValida_sinViolaciones() {
        LineaPedido l = new LineaPedido("Teclado", 2, 89.99);
        assertThat(validator.validate(l)).isEmpty();
    }

    @Test
    void validacion_productoBlanco_generaViolacion() {
        LineaPedido l = new LineaPedido("   ", 2, 89.99);
        Set<ConstraintViolation<LineaPedido>> violations = validator.validate(l);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("producto");
    }

    @Test
    void validacion_cantidadCero_generaViolacion() {
        LineaPedido l = new LineaPedido("Teclado", 0, 89.99);
        Set<ConstraintViolation<LineaPedido>> violations = validator.validate(l);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("cantidad");
    }

    @Test
    void validacion_precioNulo_generaViolacion() {
        LineaPedido l = new LineaPedido("Teclado", 2, null);
        Set<ConstraintViolation<LineaPedido>> violations = validator.validate(l);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("precioUnitario");
    }
}
