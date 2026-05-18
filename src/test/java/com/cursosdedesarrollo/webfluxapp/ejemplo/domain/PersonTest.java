package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.PersonDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Constructores ────────────────────────────────────────────────────────

    @Test
    void constructorVacio_creaInstanciaConCamposNulos() {
        Person p = new Person();
        assertThat(p.getId()).isNull();
        assertThat(p.getName()).isNull();
        assertThat(p.getLastName()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Person p = new Person("1", "David", "Vaquero");
        assertThat(p.getId()).isEqualTo("1");
        assertThat(p.getName()).isEqualTo("David");
        assertThat(p.getLastName()).isEqualTo("Vaquero");
    }

    @Test
    void constructorDesdeDTO_asignaIdFijoYCopiaNameYLastName() {
        PersonDTO dto = new PersonDTO("Lorena", "Reyes");
        Person p = new Person(dto);
        assertThat(p.getId()).isEqualTo("0L");
        assertThat(p.getName()).isEqualTo("Lorena");
        assertThat(p.getLastName()).isEqualTo("Reyes");
    }

    // ── Getters y setters ────────────────────────────────────────────────────

    @Test
    void settersYGetters_funcionanCorrectamente() {
        Person p = new Person();
        p.setId("42");
        p.setName("Carlos");
        p.setLastName("Lopez");
        assertThat(p.getId()).isEqualTo("42");
        assertThat(p.getName()).isEqualTo("Carlos");
        assertThat(p.getLastName()).isEqualTo("Lopez");
    }

    // ── equals y hashCode ────────────────────────────────────────────────────

    @Test
    void equals_dosPerssonasConMismosDatos_sonIguales() {
        Person p1 = new Person("1", "David", "Vaquero");
        Person p2 = new Person("1", "David", "Vaquero");
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void equals_personasConDistintoId_noSonIguales() {
        Person p1 = new Person("1", "David", "Vaquero");
        Person p2 = new Person("2", "David", "Vaquero");
        assertThat(p1).isNotEqualTo(p2);
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_contieneLosCampos() {
        Person p = new Person("1", "David", "Vaquero");
        String s = p.toString();
        assertThat(s).contains("1", "David", "Vaquero");
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    @Test
    void validacion_personaValida_sinViolaciones() {
        Person p = new Person("1", "David", "Vaquero");
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test
    void validacion_nameNulo_generaViolacion() {
        Person p = new Person("1", null, "Vaquero");
        Set<ConstraintViolation<Person>> violations = validator.validate(p);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameMenorDe4Caracteres_generaViolacion() {
        Person p = new Person("1", "Ana", "Vaquero");
        Set<ConstraintViolation<Person>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameMayorDe20Caracteres_generaViolacion() {
        Person p = new Person("1", "A".repeat(21), "Vaquero");
        Set<ConstraintViolation<Person>> violations = validator.validate(p);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameEnLimite4Caracteres_esValido() {
        Person p = new Person("1", "Juan", "Vaquero");
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test
    void validacion_nameEnLimite20Caracteres_esValido() {
        Person p = new Person("1", "A".repeat(20), "Vaquero");
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test
    void validacion_lastNameNulo_generaViolacion() {
        Person p = new Person("1", "David", null);
        Set<ConstraintViolation<Person>> violations = validator.validate(p);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("lastName");
    }
}
