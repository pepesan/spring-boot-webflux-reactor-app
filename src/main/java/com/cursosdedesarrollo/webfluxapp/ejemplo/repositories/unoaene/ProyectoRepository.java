package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProyectoRepository extends ReactiveMongoRepository<Proyecto, String> {}
