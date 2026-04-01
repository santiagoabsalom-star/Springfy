package com.surrogate.springfy.services.bussines;

import com.surrogate.springfy.models.DTO.Message;
import com.surrogate.springfy.websocket.MessageWebSocketHandler;
import com.surrogate.springfy.websocket.StreamWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
@RequiredArgsConstructor
@Component
public class StreamListener {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final StreamWebSocketHandler streamWebSocketHandler;

    @Async
    @EventListener
    public void handle(Message message) throws IOException {
    if(message.state!=null){
        messageWebSocketHandler.sendInitMessage(message.state, message.usuario);
    }else if(message.message!=null){
        streamWebSocketHandler.handleMessage(message.session, message.message);
    }
    else if(message.closeStatus!=null){
        streamWebSocketHandler.afterConnectionClosed(message.session, message.closeStatus);
    }
    }
}

