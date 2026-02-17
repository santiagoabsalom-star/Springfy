package com.surrogate.springfy.models.bussines.streaming;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
@Getter
@Setter

public class ClientState {
    public  WebSocketSession a;
    public ExecutorService executorService = Executors.newSingleThreadExecutor();
    public  WebSocketSession s;
    private volatile boolean stopped;
    public volatile long byteOffset = 0;

    public Control control;
    public volatile boolean change;
    public volatile boolean repeating;
    //todo hacer todos los atributos atomicos
   public String usuario, seguidor,anfitrion;

    public final BlockingQueue<BinaryMessage> backlog = new LinkedBlockingQueue<>(50);
public ClientState(WebSocketSession a) {
    this.a = a;
}


}

