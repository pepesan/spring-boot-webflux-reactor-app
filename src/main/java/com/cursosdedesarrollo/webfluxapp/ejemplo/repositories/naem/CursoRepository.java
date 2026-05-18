package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.naem;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.naem.Curso;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CursoRepository extends ReactiveMongoRepository<Curso, String> {}
