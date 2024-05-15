package com.cursosdedesarrollo.webfluxapp.ejemplo;

import com.cursosdedesarrollo.webfluxapp.ejemplo.domain.Persona;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReactorExample {
    public static void main(String[] args) throws InterruptedException {
        // defino el mono de string
        Mono<String> mensaje=Mono.just("hola");
        // asigno un método al subscribe
        mensaje.subscribe(System.out::println);

        // defino el mono con un tipo personalizado
        Mono<Persona> mensaje2=Mono.just(new Persona("David","Vaquero",44));
        // asigno un método al subscribe
        mensaje2.subscribe(System.out::println);

        Mono<Persona> mensaje3=Mono.just(new Persona("pepe","perez",20))
                .delayElement(Duration.ofSeconds(1));

        mensaje.subscribe(System.out::println);

        Thread.sleep(2000);

        System.out.println("Flux");
        Flux<String> mensaje4=Flux.just("hola" ,"que" ,"tal","estas","tu");

        mensaje4.subscribe(System.out::println);


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
        numeros3.subscribe(combi -> System.out.println("Dato emitido: " + combi),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );
        Flux<Integer> primerosTres = numeros3.take(3);
        primerosTres.subscribe(combi -> System.out.println("Dato emitido: " + combi),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Flux completado")
        );


        // Uso de multiples subscribers
        System.out.println("multiples subscribers");
        Flux<Integer> flux2 = Flux.range(1, 5);

        flux2.subscribe(i -> System.out.println("Subscriber 1: " + i));

        // Esta nueva subscripción recibirá los elementos desde el inicio.
        flux2.subscribe(i -> System.out.println("Subscriber 2: " + i));
        // Hot vs Cold Publishers
        // Ref: https://www.vinsguru.com/reactor-hot-publisher-vs-cold-publisher/
        // Cold Publisher
        // Sólo envian datos si hay un subscriptor nuevo
        // comportamiento por defecto
        System.out.println("Cold Publisher sin Subscriber");
        Mono<Integer> enteros = Mono.fromSupplier(() -> getDataToBePublished());

        Thread.sleep(2000);
        System.out.println("Cold Publisher con Subscriber");
        Mono.fromSupplier(() -> getDataToBePublished())
                .subscribe(i -> System.out.println("Observer-1 :: " + i));
        Thread.sleep(2000);
        System.out.println("Cold Publisher sólo genera si hay Subscriber");
        // Hot Publisher
        // Envían dato aunque no haya un subscriptor
        System.out.println("Hot Publisher sin Subscriber");
        // la clave está en el share que obliga a ir emitiendo datos
        Flux<String> movieTheatre = Flux.fromStream(() -> getMovie())
                .delayElements(Duration.ofSeconds(2)).share();

        // you start watching the movie
        movieTheatre.subscribe(scene -> System.out.println("You are watching " + scene));
        Thread.sleep(5000);
        // meto otro sub
        movieTheatre.subscribe(scene -> System.out.println("Vinsguru is watching " + scene));
        Thread.sleep(2000);
        System.out.println("Hot Publisher con Subscriber y delay");
        Flux<Long> hotFlux = Flux.interval(Duration.ofSeconds(1)) // Genera valores cada segundo
                .publish() // Convierte el flujo en un flujo caliente
                .autoConnect(); // Conecta el flujo para que comience a emitir

        // Simulación de un suscriptor que se une después de 3 segundos
        Thread.sleep(3000); // Espera 3 segundos
        hotFlux.subscribe(i -> System.out.println("Subscriber 1: " + i));

        // Simulación de otro suscriptor que se une después de 5 segundos
        Thread.sleep(2000); // Espera 2 segundos más
        hotFlux.subscribe(i -> System.out.println("Subscriber 2: " + i));

        Thread.sleep(5000); // Espera 5 segundos más para ver la emisión continua
    }

    private static int getDataToBePublished(){
        System.out.println("getDataToBePublished was called");
        return 1;
    }
    private static Stream<String> getMovie(){
        System.out.println("Got the movie streaming request");
        return Stream.of(
                "scene 1",
                "scene 2",
                "scene 3",
                "scene 4",
                "scene 5"
        );
    }
}
