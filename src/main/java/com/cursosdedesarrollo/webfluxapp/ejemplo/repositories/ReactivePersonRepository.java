package com.cursosdedesarrollo.webfluxapp.ejemplo.repositories;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactivePersonRepository extends ReactiveMongoRepository<Person, String> {
    // @Query("{'name': ?0}")
    Flux<Person> findByName(String name, Pageable pageable);
    // resultados
    Flux<Person> findByNameOrderByLastName(String name, Pageable pageable);
}
