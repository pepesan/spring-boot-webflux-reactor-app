package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.PersonDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "personas")
public class Person {
    @Id
    private String id;
    @NotNull
    @Size(min = 4, max = 20, message = "Debe tener entre 4 y 20 caracteres")
    private String name;
    @NotNull
    private String lastName;
    public Person(PersonDTO personDTO){
        this.setId("0L");
        this.setName(personDTO.getName());
        this.setLastName(personDTO.getLastName());
    }
}
