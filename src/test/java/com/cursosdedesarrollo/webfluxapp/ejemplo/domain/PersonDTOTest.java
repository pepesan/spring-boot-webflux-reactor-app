package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── Constructores ────────────────────────────────────────────────────────

    @Test
    void constructorVacio_creaInstanciaConCamposNulos() {
        PersonDTO dto = new PersonDTO();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getLastName()).isNull();
    }

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        PersonDTO dto = new PersonDTO("Lorena", "Reyes");
        assertThat(dto.getName()).isEqualTo("Lorena");
        assertThat(dto.getLastName()).isEqualTo("Reyes");
    }

    // ── Getters y setters ────────────────────────────────────────────────────

    @Test
    void settersYGetters_funcionanCorrectamente() {
        PersonDTO dto = new PersonDTO();
        dto.setName("Carlos");
        dto.setLastName("Garcia");
        assertThat(dto.getName()).isEqualTo("Carlos");
        assertThat(dto.getLastName()).isEqualTo("Garcia");
    }

    // ── equals y hashCode ────────────────────────────────────────────────────

    @Test
    void equals_dosDTOsConMismosDatos_sonIguales() {
        PersonDTO dto1 = new PersonDTO("David", "Vaquero");
        PersonDTO dto2 = new PersonDTO("David", "Vaquero");
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void equals_DTOsConDistintoName_noSonIguales() {
        PersonDTO dto1 = new PersonDTO("David", "Vaquero");
        PersonDTO dto2 = new PersonDTO("Maria", "Vaquero");
        assertThat(dto1).isNotEqualTo(dto2);
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_contieneLosCampos() {
        PersonDTO dto = new PersonDTO("David", "Vaquero");
        assertThat(dto.toString()).contains("David", "Vaquero");
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    @Test
    void validacion_DTOValido_sinViolaciones() {
        PersonDTO dto = new PersonDTO("David", "Vaquero");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void validacion_nameNulo_generaViolacion() {
        PersonDTO dto = new PersonDTO(null, "Vaquero");
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(dto);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameMenorDe4Caracteres_generaViolacion() {
        PersonDTO dto = new PersonDTO("Ana", "Vaquero");
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameMayorDe20Caracteres_generaViolacion() {
        PersonDTO dto = new PersonDTO("A".repeat(21), "Vaquero");
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("name");
    }

    @Test
    void validacion_nameEnLimite4Caracteres_esValido() {
        PersonDTO dto = new PersonDTO("Juan", "Vaquero");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void validacion_nameEnLimite20Caracteres_esValido() {
        PersonDTO dto = new PersonDTO("A".repeat(20), "Vaquero");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void validacion_lastNameNulo_generaViolacion() {
        PersonDTO dto = new PersonDTO("David", null);
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(dto);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("lastName");
    }
}
