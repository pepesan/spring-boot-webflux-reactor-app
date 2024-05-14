package com.cursosdedesarrollo.webfluxapp;


import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import reactor.core.publisher.Mono;

// TODO: corregir test de integración
@DataJpaTest
public class PersonRepositoryIntegrationTest{
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ReactivePersonRepository personRepository;

    @Test
    public void it_should_save_user() {
        Person person= new Person() ;
        person.setName("Pepe");
        person = entityManager.persistAndFlush(person);
        final String id = person.getId();
        Mono<Person> persisted=personRepository.findById(person.getId());
        Assertions.assertThat(persisted).isNotNull();
        persisted.subscribe(persona ->{
            Assertions.assertThat(persona).isNotNull();
            Assertions.assertThat(persona.getId()).isEqualTo(id);
            entityManager.remove(persona);
        });
    }

}