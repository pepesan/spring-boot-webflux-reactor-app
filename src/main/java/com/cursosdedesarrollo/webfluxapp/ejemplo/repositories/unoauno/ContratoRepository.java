package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoauno;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoauno.Contrato;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ContratoRepository extends ReactiveMongoRepository<Contrato, String> {}
