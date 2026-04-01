package com.surrogate.springfy.models.DTO;


import com.surrogate.springfy.models.bussines.streaming.ClientState;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;


public class Message {
    public WebSocketSession session;
    public WebSocketMessage<?> message;
    public CloseStatus closeStatus;
    public ClientState state;
    public String usuario;
    public Message(WebSocketSession session, WebSocketMessage<?> message) {
        this.session = session;
        this.message = message;
    }

    public Message(ClientState state, String usuario) {
        this.state = state;
        this.usuario = usuario;
    }
}
