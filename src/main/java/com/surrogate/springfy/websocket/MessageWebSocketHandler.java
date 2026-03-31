//package com.surrogate.springfy.websocket;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.WebSocketMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class MessageWebSocketHandler implements WebSocketHandler {
//    //TODO: Implementar messageWebsocket handler dedicado para mensajes
//    // El streamwebsockethandler solo va a quedar para sonido:D
//    //todo esto se hace para evitar mensajes solapados, a pesar de que sea concurrente la mierda nunca se sabe cuando va a fallar
//    // y tambien queremos que sea inmediato, nada de queues ni esas mierdas
//    private final StreamWebSocketHandler streamWebSocketHandler;
//    @Override
//    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
//
//    }
//
//    @Override
//    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
//        //if message== start{
//        // streamWebsockethandler.handleMessage(session, message):D
//        // }
//
//    }
//
//    @Override
//    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {
//
//    }
//
//    @Override
//    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
//
//    }
//
//    @Override
//    public boolean supportsPartialMessages() {
//        return true;
//    }
//}
