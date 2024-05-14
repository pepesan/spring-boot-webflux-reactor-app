package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Persona {
    private String nombre;
    private String apellidos;
    private int edad;
}
