package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonaTest {

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructorCompleto_asignaTodosLosCampos() {
        Persona persona = new Persona("David", "Vaquero", 35);
        assertThat(persona.getNombre()).isEqualTo("David");
        assertThat(persona.getApellidos()).isEqualTo("Vaquero");
        assertThat(persona.getEdad()).isEqualTo(35);
    }

    // ── Getters y setters ────────────────────────────────────────────────────

    @Test
    void settersYGetters_funcionanCorrectamente() {
        Persona persona = new Persona("X", "X", 0);
        persona.setNombre("Maria");
        persona.setApellidos("Lopez");
        persona.setEdad(28);
        assertThat(persona.getNombre()).isEqualTo("Maria");
        assertThat(persona.getApellidos()).isEqualTo("Lopez");
        assertThat(persona.getEdad()).isEqualTo(28);
    }

    // ── equals y hashCode ────────────────────────────────────────────────────

    @Test
    void equals_dosPersonasConMismosDatos_sonIguales() {
        Persona p1 = new Persona("David", "Vaquero", 35);
        Persona p2 = new Persona("David", "Vaquero", 35);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void equals_personasConDistintaEdad_noSonIguales() {
        Persona p1 = new Persona("David", "Vaquero", 35);
        Persona p2 = new Persona("David", "Vaquero", 40);
        assertThat(p1).isNotEqualTo(p2);
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_contieneLosCampos() {
        Persona persona = new Persona("David", "Vaquero", 35);
        assertThat(persona.toString()).contains("David", "Vaquero", "35");
    }
}
