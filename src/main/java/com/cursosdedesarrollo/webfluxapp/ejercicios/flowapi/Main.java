package com.cursosdedesarrollo.webfluxapp.ejercicios.flowapi;

import java.util.concurrent.Flow.*;
import java.util.concurrent.SubmissionPublisher;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Crear un Publisher
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        // Crear un Subscriber
        Subscriber<String> subscriber = new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1); // Solicitar los datos
            }

            @Override
            public void onNext(String item) {
                // Modificar el dato recibido
                String modifiedItem = item.toUpperCase();

                // Imprimir el dato modificado
                System.out.println("Dato modificado: " + modifiedItem);

                subscription.request(1); // Solicitar más datos
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("¡Todos los datos han sido procesados!");
            }
        };

        // Suscribir el Subscriber al Publisher
        publisher.subscribe(subscriber);

        // Publicar algunos datos
        publisher.submit("dato 1");
        publisher.submit("dato 2");
        publisher.submit("dato 3");

        // Cerrar el Publisher
        publisher.close();

        // Esperar un momento para que los datos sean procesados
        Thread.sleep(1000);
    }
}

