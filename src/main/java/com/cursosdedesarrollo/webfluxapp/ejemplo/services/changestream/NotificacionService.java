package com.cursosdedesarrollo.webfluxapp.ejemplo.services.changestream;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.changestream.Notificacion;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.changestream.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<Notificacion> findAll() {
        return notificacionRepository.findAll();
    }

    public Mono<Notificacion> create(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    /**
     * Suscribe al Change Stream de la colección notificaciones.
     * Cada vez que se inserta, actualiza o elimina un documento, MongoDB emite un evento.
     * El Flux resultante es infinito (push) mientras la conexión permanezca abierta.
     *
     * Requiere MongoDB con Replica Set o Sharded Cluster.
     * En standalone MongoDB el Change Stream lanza una excepción al suscribirse.
     */
    public Flux<Notificacion> stream() {
        return reactiveMongoTemplate
                .changeStream("notificaciones", ChangeStreamOptions.empty(), Notificacion.class)
                .mapNotNull(event -> event.getBody());
    }
}
