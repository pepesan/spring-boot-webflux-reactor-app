package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Matriculacion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MatriculacionRepository extends ReactiveMongoRepository<Matriculacion, String> {
    Flux<Matriculacion> findByEstudianteId(String estudianteId);
    Flux<Matriculacion> findByCursoId(String cursoId);
    Mono<Matriculacion> findByEstudianteIdAndCursoId(String estudianteId, String cursoId);
    Mono<Void> deleteByEstudianteIdAndCursoId(String estudianteId, String cursoId);
}
