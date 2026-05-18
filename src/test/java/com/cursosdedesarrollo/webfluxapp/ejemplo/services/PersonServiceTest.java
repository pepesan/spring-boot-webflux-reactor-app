package com.cursosdedesarrollo.webfluxapp.ejemplo.services;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Person;
import com.cursosdedesarrollo.webfluxapp.ejemplo.repositories.ReactivePersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private ReactivePersonRepository personRepository;

    @InjectMocks
    private PersonService service;

    private Person david;

    @BeforeEach
    void setUp() {
        david = new Person("1", "David", "Vaquero");
    }

    @Test
    void findAll_returnsOkResponseWithPersons() {
        when(personRepository.findAll()).thenReturn(Flux.just(david));
        ServerRequest request = mock(ServerRequest.class);

        StepVerifier.create(service.findAll(request))
                .expectNextMatches(resp -> resp.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void findAll_emptyRepo_returnsOkResponseWithEmptyBody() {
        when(personRepository.findAll()).thenReturn(Flux.empty());
        ServerRequest request = mock(ServerRequest.class);

        StepVerifier.create(service.findAll(request))
                .expectNextMatches(resp -> resp.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void findPerson_existingId_returnsOkResponse() {
        when(personRepository.findById("1")).thenReturn(Mono.just(david));
        ServerRequest request = mock(ServerRequest.class);
        when(request.pathVariable("id")).thenReturn("1");

        StepVerifier.create(service.findPerson(request))
                .expectNextMatches(resp -> resp.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void savePerson_returnsOkResponseWithIdAndName() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(Person.class)).thenReturn(Mono.just(david));

        Mono<ServerResponse> response = service.savePerson(request);

        StepVerifier.create(response)
                .expectNextMatches(resp -> resp.statusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void loadAllPersonsStream_returnsPersonsFlux() {
        when(personRepository.findAll()).thenReturn(Flux.just(david));

        StepVerifier.create(service.loadAllPersonsStream())
                .expectNext(david)
                .verifyComplete();
    }

    @Test
    void loadAllPersonsStream_emptyRepo_returnsEmptyFlux() {
        when(personRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(service.loadAllPersonsStream())
                .verifyComplete();
    }

    @Test
    void getPersons_returnsSseResponseWithOkStatus() {
        when(personRepository.findAll()).thenReturn(Flux.just(david));
        ServerRequest request = mock(ServerRequest.class);

        StepVerifier.create(service.getPersons(request))
                .expectNextMatches(resp ->
                        resp.statusCode() == HttpStatus.OK &&
                        resp.headers().getContentType() != null &&
                        resp.headers().getContentType().isCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .verifyComplete();
    }
}
