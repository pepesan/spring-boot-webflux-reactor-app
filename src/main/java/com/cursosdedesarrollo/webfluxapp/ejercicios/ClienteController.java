package com.cursosdedesarrollo.webfluxapp.ejercicios;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Clientes (MongoDB)", description = "CRUD de clientes persistido en MongoDB (colección 'clientes'). nombre es obligatorio.")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;

    @Operation(summary = "Listar todos los clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    @GetMapping
    public Flux<Cliente> getAll(){
        return this.clienteRepository.findAll();
    }

    @Operation(summary = "Crear un cliente", description = "nombre es obligatorio y no puede estar en blanco.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente creado",
                content = @Content(schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    })
    @PostMapping
    public Mono<ResponseEntity<Cliente>> postOne(@Valid @RequestBody Cliente cliente){
        return this.clienteRepository.save(cliente)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                content = @Content(schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> getById(
            @Parameter(description = "ID MongoDB del cliente") @PathVariable("id") String id){
        return this.clienteRepository.findById(id)
                .map(cliente -> new ResponseEntity<Cliente>(cliente, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @Operation(summary = "Actualizar cliente por ID", description = "Actualiza todos los campos del cliente. Usa POST (no PUT) por convención del ejercicio.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente actualizado",
                content = @Content(schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    @PostMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> postById(
            @Parameter(description = "ID MongoDB del cliente") @PathVariable("id") String id,
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

    @Operation(summary = "Eliminar cliente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente eliminado",
                content = @Content(schema = @Schema(implementation = Cliente.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> deleteById(
            @Parameter(description = "ID MongoDB del cliente") @PathVariable("id") String id){
        return this.clienteRepository.findById(id)
                .flatMap(cliente ->
                        clienteRepository.delete(cliente)
                                .then(Mono.just(
                                                new ResponseEntity<Cliente>(cliente, HttpStatus.OK)))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
