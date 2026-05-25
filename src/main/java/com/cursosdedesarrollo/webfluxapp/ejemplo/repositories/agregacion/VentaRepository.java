package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.agregacion;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.agregacion.Venta;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VentaRepository extends ReactiveMongoRepository<Venta, String> {}
