package com.cursosdedesarrollo.webfluxapp.ejemplo.websocket;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;

public class WebSocketHandler extends AbstractWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("New Text Message Received");
        System.out.println(message);
        // procesado del dato de entrada obtener la información
        Integer action = 0;
        // toma decisiones respecto a los datos de entrada
        if (message.toString().equals("action")){
            System.out.println("action");
        }
        // procesar la petición

        // devolver la información
        session.sendMessage(message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        System.out.println("New Binary Message Received");
        session.sendMessage(message);
    }
}