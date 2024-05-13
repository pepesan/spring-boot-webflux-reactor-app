package com.cursosdedesarrollo.webfluxapp.ejercicios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Data
@Document(collection = "clientes")
public class Cliente {
    @Id
    public String id;

    @NotNull
    @NotBlank
    public String nombre;

    public String direccion;

    public String tlf;

    public String correo;

}
