package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Empleado;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EmpleadoRepository extends ReactiveMongoRepository<Empleado, String> {}
