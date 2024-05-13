package com.cursosdedesarrollo.webfluxapp.ejercicios;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public Flux<Cliente> getAll(){
        return this.clienteRepository.findAll();
    }

    @PostMapping
    public Mono<ResponseEntity<Cliente>> postOne(@Valid @RequestBody Cliente cliente){
        return this.clienteRepository.save(cliente)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> getById(@PathVariable("id") String id){
        return this.clienteRepository.findById(id)
                .map(cliente -> new ResponseEntity<Cliente>(cliente, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PostMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> postById(
            @PathVariable("id") String id,
            @Valid @RequestBody Cliente cliente){
        return this.clienteRepository.findById(id)
                // ya teniendo el objeto obtenido desde la bbdd
                .flatMap(clienteGuardado -> {
                    clienteGuardado.setNombre(cliente.getNombre());
                    clienteGuardado.setDireccion(cliente.getDireccion());
                    clienteGuardado.setTlf(cliente.getTlf());
                    clienteGuardado.setCorreo(cliente.getCorreo());
                    return this.clienteRepository.save(clienteGuardado);
                })
                // genero la ResponseEntity
                .map(clienteActualizado -> new ResponseEntity<>(clienteActualizado, HttpStatus.OK))
                // En el caso de no encontrarlo
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> deleteById(@PathVariable("id") String id){
        return this.clienteRepository.findById(id)
                .flatMap(cliente ->
                        clienteRepository.delete(cliente)
                                .then(Mono.just(
                                                new ResponseEntity<Cliente>(cliente, HttpStatus.OK)))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
