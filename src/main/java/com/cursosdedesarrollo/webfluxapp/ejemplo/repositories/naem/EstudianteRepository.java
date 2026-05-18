package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Estudiante;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EstudianteRepository extends ReactiveMongoRepository<Estudiante, String> {}
