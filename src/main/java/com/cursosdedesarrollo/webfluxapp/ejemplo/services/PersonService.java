package com.cursosdedesarrollo.webfluxapp.ejemplo.services;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PersonService {
    @Autowired
    private ReactivePersonRepository personRepository;

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        Flux<Person> customerList = this.personRepository.findAll();
        return ServerResponse.ok().body(customerList,Person.class);
    }
    public Mono<ServerResponse> findPerson(ServerRequest request){
        String personId= request.pathVariable("id");
        Mono<Person> personMono = this.personRepository.findById(personId);
        return ServerResponse.ok().body(personMono,Person.class);
    }
    public Mono<ServerResponse> savePerson(ServerRequest request){
        Mono<Person> personMono = request.bodyToMono(Person.class);
        Mono<String> saveResponse = personMono.map(dto -> dto.getId() + ":" + dto.getName());
        return ServerResponse.ok().body(saveResponse,String.class);
    }
    public Flux<Person> loadAllPersonsStream() {
        long start = System.currentTimeMillis();
        Flux<Person> persons = this.personRepository.findAll();
        long end = System.currentTimeMillis();
        System.out.println("Total execution time : " + (end - start));
        return persons;
    }
    public Mono<ServerResponse> getPersons(ServerRequest request) {

        Flux<Person> personsStream = this.loadAllPersonsStream();
        return ServerResponse.ok().
                contentType(MediaType.TEXT_EVENT_STREAM)
                .body(personsStream, Person.class);
    }
}
