package com.cursosdedesarrollo.webfluxapp.ejercicios.reactor;

public class Main {
    public static void main(String[] args) {
        // Crear y ejecutar el Flux
        StringFluxCreator.createStringFlux()
                .subscribe(
                        data -> System.out.println("Dato modificado: " + data),
                        error -> error.printStackTrace(),
                        () -> System.out.println("¡Todos los datos han sido procesados!")
                );
    }
}
