package com.surrogate.springfy.models.bussines.streaming;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter

public class ClientState {
    public  WebSocketSession a;
    public ExecutorService executorService = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());

    public  WebSocketSession s;
    public long bytesSentTotal;
    public volatile boolean stopped;
    public volatile long startTime;
    public volatile int byteOffset = 0;
    public volatile boolean started;
    public String currentSongId;
    public int currentPosition;
    public Control control;
    public int currentSongDuration;
    public List<String> currentPlaylist;
    public volatile boolean change;
    public volatile boolean repeating;

    //todo hacer todos los atributos atomicos
   public String usuario, seguidor,anfitrion;

public ClientState(WebSocketSession a) {
    this.a = a;
}


}

