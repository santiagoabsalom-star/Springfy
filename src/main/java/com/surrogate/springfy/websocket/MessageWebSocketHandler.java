package com.surrogate.springfy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surrogate.springfy.models.DTO.Message;
import com.surrogate.springfy.models.bussines.streaming.ClientState;
import com.surrogate.springfy.models.bussines.streaming.Comando;
import com.surrogate.springfy.repositories.bussines.DuoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageWebSocketHandler implements WebSocketHandler {
    //TODO: Implementar messageWebsocket handler dedicado para mensajes
    // El streamwebsockethandler solo va a quedar para sonido:D
    //todo esto se hace para evitar mensajes solapados, a pesar de que sea concurrente la mierda nunca se sabe cuando va a fallar
    // y tambien queremos que sea inmediato, nada de queues ni esas mierdas
    private final ApplicationEventPublisher publisher;
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final DuoRepository duoRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws IOException {
        String usuario = (String) session.getAttributes().get("Usuario");
        log.info("Usuario en sesion es: {} ",  usuario);
        if(Objects.nonNull(usuario)) {

            sessions.put(usuario, session);
            String duoUsername = duoRepository.getOtherUsername(usuario);
            if (sessions.containsKey(duoUsername)) {
                Comando commando = new Comando();;
                commando.setComando("duo-connected");
                String js = mapper.writeValueAsString(commando);
                TextMessage textMessage = new TextMessage(js);
                log.info(commando.getComando());
                session.sendMessage(textMessage);
                sessions.get(duoUsername).sendMessage(textMessage);
            }
        }

        else{
            session.close(new CloseStatus(404, "Usuario no encontrado"));
        }
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
        String usuario = (String) session.getAttributes().get("Usuario");
        String json = (String) message.getPayload();
        Comando comando = mapper.readValue(json, Comando.class);
        String command = comando.getComando();
        switch (command) {
            case "follower-connect", "follower-disconnect",
                 "start", "stop",
                 "resume","change",
                 "move","repeat",
                 "disconnect"-> publisher.publishEvent(new Message(session,message));
            case "emoji" -> {
                String duoUsername = duoRepository.getOtherUsername(usuario);
                WebSocketSession anfitrion = sessions.get(duoUsername);
                if (Objects.nonNull(anfitrion)) {
                    anfitrion.sendMessage(message);
                }
            }
        }
        if(command.equals("is-duo-connected")) {
            log.info("Recibida la verificacion de conexion de duo");
            String duoUsername = duoRepository.getOtherUsername(usuario);
            if (duoUsername != null) {

                if (sessions.containsKey(duoUsername)) {
                    Comando commando = new Comando();
                    commando.setComando("duo-connected");
                    String js = mapper.writeValueAsString(commando);
                    log.info("Enviando verificacion de conexion de duo");
                    log.info(comando.getComando());
                    session.sendMessage(new TextMessage(js));
                }
                return;
            }
            return;

        }


    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
        String usuario = (String) session.getAttributes().get("Usuario");
        sessions.remove(usuario);

    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    public void sendInitMessage(ClientState state, String usuario) throws IOException {
        String duoUsername = duoRepository.getOtherUsername(usuario);

        if (duoUsername != null) {

            WebSocketSession session = sessions.get(usuario);
            WebSocketSession duo = sessions.get(duoUsername);

            if(Objects.nonNull(duo) && duo.isOpen() && session != null) {

                Comando comando = new Comando();
                comando.setComando("duo-connected");
                String json = mapper.writeValueAsString(comando);
                duo.sendMessage(new TextMessage(json));

                if (Objects.nonNull(state)) {
                    if (state.started) {
                        log.info("Avisando a {} que ya esta iniciada la musica" ,usuario);
                        Comando duration = new Comando();
                        duration.setComando("duration");
                        duration.setDuration(state.currentSongDuration);
                        String jsonDuration = mapper.writeValueAsString(duration);

                        session.sendMessage(new TextMessage(jsonDuration));

                        Comando command = new Comando();
                        command.setComando("start");
                        command.setPlaying(!state.control.pausado);
                        command.setAnfitrion(state.anfitrion);
                        command.setSeguidor(state.seguidor);
                        command.setRepeating(state.repeating);
                        command.setMusicId(state.currentSongId);
                        command.setCurrentPlaylist(state.currentPlaylist);
                        command.setCurrentPosition(state.currentPosition);
                        String sw = mapper.writeValueAsString(command);
                        session.sendMessage(new TextMessage(sw));
                    }
                }
            }
        }
    }
}
