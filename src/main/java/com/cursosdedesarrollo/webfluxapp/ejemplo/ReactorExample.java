package com.cursosdedesarrollo.webfluxapp.ejemplo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class ReactorExample {
    public static void main(String[] args) {
        // Crear un Mono con un valor inicial
        Mono<String> mono = Mono.just("Hola");

        // Aplicar la primera transformación y suscribirse al resultado
        Mono<String> monoModificado1 = mono.map(valor -> valor + " mundo!");
        monoModificado1.subscribe(
                valor -> System.out.println("Transformación 1: " + valor),
                error -> System.err.println("Error en transformación 1: " + error),
                () -> System.out.println("Transformación 1 completada")
        );

        // Aplicar la segunda transformación y suscribirse al resultado
        Mono<String> monoModificado2 = mono.map(valor -> valor.toUpperCase());
        monoModificado2.subscribe(
                valor -> System.out.println("Transformación 2: " + valor),
                error -> System.err.println("Error en transformación 2: " + error),
                () -> System.out.println("Transformación 2 completada")
        );

        // Crear un Flux a partir de una lista
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);

        // Suscribirse al Flux y ejecutar un método cada vez que se emite un elemento
        flux.subscribe(
                valor -> {
                    // Aquí ejecuta el método que deseas cada vez que se emite un elemento
                    System.out.println("Elemento emitido: " + valor);
                    // Por ejemplo, podrías llamar a un método en esta sección
                    // que realice alguna acción basada en el valor emitido
                },
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );



        // manipulaciones
        // flatmap
        // Permite transformar el valor de un Mono en otro Mono.
        // Útil cuando necesitas realizar una operación asíncrona dentro de una transformación
        // y obtener otro Mono como resultado.
        Mono<Integer> mono2 = Mono.just(2);
        Mono<String> resultado = mono2.flatMap(valor -> Mono.just("El doble es: " + valor * 2));
        resultado.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación 1: " + error),
                () -> System.out.println("Transformación 1 completada")
        );
        // con esto se bloquea el hilo de ejecución
        // String dato = resultado.block();
        // System.out.println("Dato extraído: " + dato);

        // defaultIfEmpty
        // Proporciona un valor predeterminado si el Mono está vacío.
        // Útil cuando deseas proporcionar un valor por defecto en caso de que
        // el Mono no emita ningún valor.
        Mono<Integer> mono4 = Mono.empty();
        Mono<Integer> resultado2 = mono4.defaultIfEmpty(0);
        resultado2.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación 1: " + error),
                () -> System.out.println("Transformación completada")
        );
        // switchIfEmpty:
        // Reemplaza un Mono vacío con otro Mono.
        // Útil cuando deseas proporcionar una fuente alternativa de valores en caso de que
        // el Mono original esté vacío.
        Mono<Integer> mono5 = Mono.empty();
        Mono<Integer> fuenteAlternativa = Mono.just(10);
        Mono<Integer> resultado3 = mono5.switchIfEmpty(fuenteAlternativa);
        resultado3.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación 1: " + error),
                () -> System.out.println("Transformación completada")
        );
        // filter:
        // Filtra los elementos emitidos por el Mono según un predicado.
        // Útil para eliminar elementos no deseados.
        Mono<Integer> mono6 = Mono.just(5);
        Mono<Integer> resultado4 = mono6.filter(valor -> valor > 3);
        resultado4.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación: " + error),
                () -> System.out.println("Transformación completada")
        );
        // zipWith:
        // Combina el valor de un Mono con el valor de otro Mono usando una función.
        // Útil para combinar los valores de dos Monos en uno solo.
        Mono<Integer> mono7 = Mono.just(2);
        Mono<Integer> mono8 = Mono.just(3);
        Mono<Integer> resultado5 = mono7.zipWith(mono8, (valor1, valor2) -> valor1 + valor2);
        resultado5.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación: " + error),
                () -> System.out.println("Transformación completada")
        );

        // Operador map:
        //
        //Transforma cada elemento emitido por el Flux.
        Flux<Integer> numeros = Flux.just(1, 2, 3, 4, 5);
        Flux<String> strings = numeros.map(numero -> "Número: " + numero);
        strings.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación: " + error),
                () -> System.out.println("Transformación completada")
        );
        // Operador filter:
        //
        //Filtra los elementos emitidos por el Flux según un predicado.
        Flux<Integer> pares = numeros.filter(numero -> numero % 2 == 0);
        pares.subscribe(
                valor -> System.out.println("Resultado: " + valor),
                error -> System.err.println("Error en transformación: " + error),
                () -> System.out.println("Transformación completada")
        );
        // Operador flatMap:
        //
        // Transforma cada elemento emitido por el Flux en otro Flux,
        // y luego combina los elementos emitidos por todos los Flux resultantes en uno solo.

        // Lista de listas de números
        List<List<Integer>> listaDeListas = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Arrays.asList(7, 8, 9)
        );

        // Crear un Flux a partir de la lista de listas
        Flux<List<Integer>> fluxDeListas = Flux.fromIterable(listaDeListas);

        // Utilizar flatMap para convertir cada lista en un Flux de números y luego combinarlos en un solo Flux
        Flux<Integer> fluxDeNumeros = fluxDeListas.flatMap(Flux::fromIterable);

        // Suscribirse al Flux resultante y manejar los números emitidos
        fluxDeNumeros.subscribe(
                numero -> System.out.println("Número emitido: " + numero),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );

        // Operador concatMap:
        //
        // Similar a flatMap, pero garantiza que los elementos de salida se emitan
        // en el mismo orden que los elementos de entrada.
        Flux<String> colores = Flux.just("rojo", "verde", "azul");
        Flux<String> coloresMayusculas = colores.concatMap(color -> Flux.just(color.toUpperCase()));
        coloresMayusculas.subscribe(color -> System.out.println("Color emitido: " + color),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );

        // Operador zipWith:
        //
        //Combina el Flux actual con otro Flux, emitiendo un par de elementos resultantes.
        Flux<Integer> numeros2 = Flux.just(1, 2, 3);
        Flux<String> letras = Flux.just("a", "b", "c");
        Flux<String> combinados = numeros2.zipWith(letras, (num, letra) -> num + "-" + letra);
        combinados.subscribe(combi -> System.out.println("Dato emitido: " + combi),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );
        // Operador take:
        //
        //Emite solo los primeros n elementos emitidos por el Flux.
        Flux<Integer> numeros3 = Flux.range(1, 10);
        Flux<Integer> primerosTres = numeros3.take(3);
        primerosTres.subscribe(combi -> System.out.println("Dato emitido: " + combi),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );
    }
}
