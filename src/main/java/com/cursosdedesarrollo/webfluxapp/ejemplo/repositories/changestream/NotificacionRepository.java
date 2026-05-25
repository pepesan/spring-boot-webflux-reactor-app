package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.changestream;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream.Notificacion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NotificacionRepository extends ReactiveMongoRepository<Notificacion, String> {}
