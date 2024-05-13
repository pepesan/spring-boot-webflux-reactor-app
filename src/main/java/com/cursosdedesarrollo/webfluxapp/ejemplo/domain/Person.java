package com.cursosdedesarrollo.webfluxapp.ejemplo.domain;

import jakarta.validation.constraints.NotNull;
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
    private String name;
    @NotNull
    private String lastName;
}
