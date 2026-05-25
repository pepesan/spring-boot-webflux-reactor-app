package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void constructorVacio_camposNulos() {
        Notificacion n = new Notificacion();
        assertThat(n.getId()).isNull();
        assertThat(n.getTitulo()).isNull();
        assertThat(n.getMensaje()).isNull();
        assertThat(n.getTipo()).isNull();
        assertThat(n.getFecha()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        LocalDateTime ahora = LocalDateTime.now();
        Notificacion n = new Notificacion("n1", "Sistema actualizado", "Versión 2.0", "INFO", ahora);
        assertThat(n.getId()).isEqualTo("n1");
        assertThat(n.getTitulo()).isEqualTo("Sistema actualizado");
        assertThat(n.getMensaje()).isEqualTo("Versión 2.0");
        assertThat(n.getTipo()).isEqualTo("INFO");
        assertThat(n.getFecha()).isEqualTo(ahora);
    }

    @Test
    void settersYGetters_funcionan() {
        LocalDateTime ahora = LocalDateTime.now();
        Notificacion n = new Notificacion();
        n.setId("n1");
        n.setTitulo("Alerta");
        n.setMensaje("Disco lleno");
        n.setTipo("ALERTA");
        n.setFecha(ahora);
        assertThat(n.getTitulo()).isEqualTo("Alerta");
        assertThat(n.getTipo()).isEqualTo("ALERTA");
    }

    @Test
    void equals_dosNotificacionesConMismosDatos_sonIguales() {
        LocalDateTime ahora = LocalDateTime.of(2024, 1, 1, 10, 0);
        Notificacion n1 = new Notificacion("n1", "Titulo", "Mensaje", "INFO", ahora);
        Notificacion n2 = new Notificacion("n1", "Titulo", "Mensaje", "INFO", ahora);
        assertThat(n1).isEqualTo(n2);
        assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
    }

    @Test
    void toString_contieneLosCampos() {
        Notificacion n = new Notificacion("n1", "Titulo", "Mensaje", "INFO", LocalDateTime.now());
        assertThat(n.toString()).contains("Titulo", "INFO");
    }

    @Test
    void validacion_notificacionValida_sinViolaciones() {
        Notificacion n = new Notificacion(null, "Titulo", "Mensaje", "INFO", LocalDateTime.now());
        assertThat(validator.validate(n)).isEmpty();
    }

    @Test
    void validacion_tituloBlanco_generaViolacion() {
        Notificacion n = new Notificacion(null, "   ", "Mensaje", "INFO", LocalDateTime.now());
        Set<ConstraintViolation<Notificacion>> violations = validator.validate(n);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("titulo");
    }

    @Test
    void validacion_mensajeBlanco_generaViolacion() {
        Notificacion n = new Notificacion(null, "Titulo", "   ", "INFO", LocalDateTime.now());
        Set<ConstraintViolation<Notificacion>> violations = validator.validate(n);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("mensaje");
    }

    @Test
    void validacion_tipoBlanco_generaViolacion() {
        Notificacion n = new Notificacion(null, "Titulo", "Mensaje", "", LocalDateTime.now());
        Set<ConstraintViolation<Notificacion>> violations = validator.validate(n);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("tipo");
    }

    @Test
    void validacion_fechaNula_generaViolacion() {
        Notificacion n = new Notificacion(null, "Titulo", "Mensaje", "INFO", null);
        Set<ConstraintViolation<Notificacion>> violations = validator.validate(n);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("fecha");
    }
}
