package com.cursosdedesarrollo.webfluxapp.ejemplo.services.unoaene;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Proyecto;
import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.unoaene.Tarea;
import com.cursosdedesarrollo.webfluxapp.ejemplo.dto.unoaene.ProyectoConTareasDTO;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene.ProyectoRepository;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.unoaene.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    // ── CRUD Proyecto ────────────────────────────────────────────────────────

    public Flux<Proyecto> findAll() {
        return proyectoRepository.findAll();
    }

    public Mono<Proyecto> create(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    // ── CRUD Tarea ───────────────────────────────────────────────────────────

    public Mono<Tarea> createTarea(Tarea tarea) {
        return proyectoRepository.existsById(tarea.getProyectoId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
                    }
                    return tareaRepository.save(tarea);
                });
    }

    public Mono<ResponseEntity<Tarea>> updateEstado(String tareaId, String estado) {
        return tareaRepository.findById(tareaId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada")))
                .flatMap(tarea -> {
                    tarea.setEstado(estado);
                    return tareaRepository.save(tarea);
                })
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<Tarea>> deleteTarea(String tareaId) {
        return tareaRepository.findById(tareaId)
                .flatMap(t -> tareaRepository.delete(t).then(Mono.just(ResponseEntity.ok(t))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ── Operaciones 1:N ──────────────────────────────────────────────────────

    /**
     * Busca el proyecto y luego emite todas sus tareas con flatMapMany.
     * flatMapMany convierte un Mono<Proyecto> en un Flux<Tarea>
     * encadenando la query derivada findByProyectoId.
     */
    public Flux<Tarea> findTareasByProyecto(String proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado")))
                .flatMapMany(p -> tareaRepository.findByProyectoId(p.getId()));
    }

    /**
     * Igual que findTareasByProyecto pero aplicando el filtro de estado
     * directamente en la query derivada de MongoDB (no en memoria).
     */
    public Flux<Tarea> findTareasByProyectoAndEstado(String proyectoId, String estado) {
        return proyectoRepository.findById(proyectoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado")))
                .flatMapMany(p -> tareaRepository.findByProyectoIdAndEstado(p.getId(), estado));
    }

    /**
     * Agrega el Flux<Tarea> en una List<Tarea> con collectList()
     * y lo combina con el proyecto en un único DTO.
     */
    public Mono<ProyectoConTareasDTO> findProyectoConTareas(String proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado")))
                .flatMap(proyecto ->
                        tareaRepository.findByProyectoId(proyectoId)
                                .collectList()
                                .map(tareas -> new ProyectoConTareasDTO(proyecto, tareas))
                );
    }

    /**
     * Borrado en cascada: elimina primero todas las tareas del proyecto
     * con flatMap sobre el Flux<Tarea>, luego elimina el proyecto con then().
     */
    public Mono<ResponseEntity<Proyecto>> deleteConTareas(String proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado")))
                .flatMap(proyecto ->
                        tareaRepository.findByProyectoId(proyectoId)
                                .flatMap(tareaRepository::delete)
                                .then(proyectoRepository.delete(proyecto))
                                .then(Mono.just(ResponseEntity.ok(proyecto)))
                );
    }
}
