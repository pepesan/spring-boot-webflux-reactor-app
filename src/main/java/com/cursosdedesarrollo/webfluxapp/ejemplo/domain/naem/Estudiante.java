package com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Schema(description = "Estudiante. Puede estar matriculado en varios cursos (relación N:M vía colección matriculaciones).")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "estudiantes")
public class Estudiante {
    @Schema(description = "ID generado por MongoDB", example = "6651a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    private String id;
    @Schema(description = "Nombre completo del estudiante", example = "Laura Martínez")
    @NotBlank
    private String nombre;
    @Schema(description = "Correo electrónico del estudiante", example = "laura@ejemplo.com")
    @NotBlank
    @Email
    private String email;
}
