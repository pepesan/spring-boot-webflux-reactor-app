package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TareaRepository extends ReactiveMongoRepository<Tarea, String> {
    Flux<Tarea> findByProyectoId(String proyectoId);
    Flux<Tarea> findByProyectoIdAndEstado(String proyectoId, String estado);
}
