package com.cursosdedesarrollo.webfluxapp.exception;

import com.cursosdedesarrollo.webfluxapp.ejemplo.controllers.PersonSimpleRestController;
import com.cursosdedesarrollo.webfluxapp.ejercicios.ClienteController;
import com.cursosdedesarrollo.webfluxapp.ejercicios.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    /** Controlador mínimo de apoyo para provocar excepciones concretas. */
    @RestController
    static class ErrorTriggerController {
        @GetMapping("/test/response-status")
        public Mono<String> throwResponseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "recurso no encontrado");
        }

        @GetMapping("/test/generic-error")
        public Mono<String> throwGeneric() {
            throw new RuntimeException("error inesperado");
        }
    }

    private WebTestClient clientSimple;
    private WebTestClient clientClientes;
    private WebTestClient clientErrors;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        clientSimple = MockMvcWebTestClient
                .bindToController(new PersonSimpleRestController())
                .controllerAdvice(new GlobalExceptionHandler())
                .build();

        clientClientes = MockMvcWebTestClient
                .bindToController(clienteController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();

        clientErrors = MockMvcWebTestClient
                .bindToController(new ErrorTriggerController())
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── MethodArgumentNotValidException ──────────────────────────────────────

    @Test
    void validacion_nameCorto_retorna400ConErrorResponseLimpio() {
        clientSimple.post().uri("/api/persons-simple")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Ana\", \"lastName\": \"Test\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Errores de validación")
                .jsonPath("$.details").isArray()
                .jsonPath("$.details[0]").isEqualTo("name: Debe tener entre 4 y 20 caracteres");
    }

    @Test
    void validacion_nameNulo_retorna400ConDetalle() {
        clientSimple.post().uri("/api/persons-simple")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"lastName\": \"Test\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details").isArray();
    }

    @Test
    void validacion_clienteNombreVacio_retorna400ConDetalle() {
        clientClientes.post().uri("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.details").isArray();
    }

    @Test
    void respuestaError_noContieneTrace() {
        clientSimple.post().uri("/api/persons-simple")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Ana\", \"lastName\": \"Test\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.trace").doesNotExist();
    }

    // ── ResponseStatusException ───────────────────────────────────────────────

    @Test
    void responseStatusException_retornaCodigoYMensajeDelError() {
        clientErrors.get().uri("/test/response-status")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("recurso no encontrado")
                .jsonPath("$.details").isArray();
    }

    @Test
    void responseStatusException_noContieneTrace() {
        clientErrors.get().uri("/test/response-status")
                .exchange()
                .expectBody()
                .jsonPath("$.trace").doesNotExist();
    }

    // ── Exception genérica ────────────────────────────────────────────────────

    @Test
    void excepcionGenerica_retorna500ConMensaje() {
        clientErrors.get().uri("/test/generic-error")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("error inesperado")
                .jsonPath("$.details").isArray();
    }

    @Test
    void excepcionGenerica_noContieneTrace() {
        clientErrors.get().uri("/test/generic-error")
                .exchange()
                .expectBody()
                .jsonPath("$.trace").doesNotExist();
    }
}
