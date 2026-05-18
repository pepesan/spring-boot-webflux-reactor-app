package com.cursosdedesarrollo.webfluxapp.ejemplo.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketHandlerTest {

    @Mock
    private WebSocketSession session;

    private final WebSocketHandler handler = new WebSocketHandler();

    @Test
    void handleTextMessage_echosMessageBackToSession() throws Exception {
        TextMessage message = new TextMessage("hola");
        handler.handleTextMessage(session, message);
        verify(session).sendMessage(message);
    }

    @Test
    void handleTextMessage_conPayloadVacio_echosMessageBack() throws Exception {
        TextMessage message = new TextMessage("");
        handler.handleTextMessage(session, message);
        verify(session).sendMessage(message);
    }

    @Test
    void handleBinaryMessage_echosMessageBackToSession() throws Exception {
        BinaryMessage message = new BinaryMessage(new byte[]{1, 2, 3});
        handler.handleBinaryMessage(session, message);
        verify(session).sendMessage(message);
    }
}
