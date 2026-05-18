package com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Proyecto con su lista completa de tareas. Resultado de agregar un Flux<Tarea> con collectList().")
public record ProyectoConTareasDTO(
        @Schema(description = "Datos del proyecto") Proyecto proyecto,
        @Schema(description = "Lista de tareas del proyecto (puede ser vacía)") List<Tarea> tareas
) {}
