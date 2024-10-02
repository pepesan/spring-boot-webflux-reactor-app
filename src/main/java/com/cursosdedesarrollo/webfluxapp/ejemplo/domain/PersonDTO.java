package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    @NotNull
    @Size(min = 4, max = 20, message = "Debe tener entre 4 y 20 caracteres")
    private String name;
    @NotNull
    private String lastName;
}
