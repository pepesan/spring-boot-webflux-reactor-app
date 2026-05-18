package com.cursosdedesarrollo.webfluxapp.ejemplo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "Temperaturas (SSE)", description = "Stream infinito de temperaturas aleatorias via Server-Sent Events.")
@RestController
public class TemperatureController {
    Logger logger = LoggerFactory.getLogger(TemperatureController.class);
    @Operation(summary = "Stream SSE de temperaturas", description = "Emite temperaturas aleatorias entre 0 y 49 °C cada segundo de forma indefinida. Consume con Accept: text/event-stream.")
    @ApiResponse(responseCode = "200", description = "Stream de enteros (temperatura en °C)",
            content = @Content(schema = @Schema(implementation = Integer.class)))
    @GetMapping(
            value = "/temperatures",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getTemperature() {
        Random r = new Random();
        int low = 0;
        int high = 50;
        return Flux.fromStream(
                Stream.generate(
                        () ->
                                // Devuelve el dato que tu quieras
                                // return cosa
                                r.nextInt(high - low) + low
                )
                .map(s -> String.valueOf(s))
                .peek((msg) -> {
                    logger.info(msg);
                }))
                .map(s -> Integer.valueOf(s))
                .delayElements(Duration.ofSeconds(1));
    }
}
